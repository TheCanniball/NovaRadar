package com.novascanner.network.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novascanner.network.localization.Strings
import com.novascanner.network.scanner.Grade
import com.novascanner.network.scanner.IpGenerator
import com.novascanner.network.scanner.ProbeResult
import com.novascanner.network.scanner.ScannerEngine
import com.novascanner.network.utils.AppSettings
import com.novascanner.network.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanHistoryEntry(
    val id: Long,
    val timestamp: String,
    val totalIps: Int,
    val workingCount: Int,
    val failedCount: Int,
    val source: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val settings = AppSettings(application)

    private val _results = MutableStateFlow<List<ProbeResult>>(emptyList())
    val results: StateFlow<List<ProbeResult>> = _results.asStateFlow()
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    private val _scanned = MutableStateFlow(0)
    val scanned: StateFlow<Int> = _scanned.asStateFlow()
    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total.asStateFlow()
    private val _failed = MutableStateFlow(0)
    val failed: StateFlow<Int> = _failed.asStateFlow()
    private val _startTime = MutableStateFlow(0L)
    val startTime: StateFlow<Long> = _startTime.asStateFlow()

    val manualIps = MutableStateFlow("")
    val cidrInput = MutableStateFlow("104.16.0.0/12")
    val port = MutableStateFlow("443")
    val threads = MutableStateFlow("30")
    val timeout = MutableStateFlow("3000")
    val sni = MutableStateFlow("speed.cloudflare.com")
    val suffix = MutableStateFlow("?ed=2560")
    val suffixOn = MutableStateFlow(true)
    val sortBy = MutableStateFlow("latency")
    val useManualIps = MutableStateFlow(true)
    val filterGradeMin = MutableStateFlow("")
    val resultsSearch = MutableStateFlow("")
    val favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    private val _scanHistory = MutableStateFlow<List<ScanHistoryEntry>>(emptyList())
    val scanHistory: StateFlow<List<ScanHistoryEntry>> = _scanHistory.asStateFlow()
    private var historyIdCounter = 0L
    private var currentScanSource = ""

    private var scanJob: Job? = null

    init {
        manualIps.value = settings.manualIps
        cidrInput.value = settings.cidr
        port.value = settings.port
        threads.value = settings.threads
        timeout.value = settings.timeout
        sni.value = settings.sni
        suffix.value = settings.suffix
        suffixOn.value = settings.suffixOn
        Strings.isRtl = settings.isRtl
        sortBy.value = settings.sortBy
    }

    fun persistSettings() {
        settings.saveAll(port.value, threads.value, timeout.value, sni.value,
            suffix.value, suffixOn.value, Strings.isRtl, manualIps.value, cidrInput.value)
    }

    fun toggleLang() {
        Strings.isRtl = !Strings.isRtl
        settings.isRtl = Strings.isRtl
        persistSettings()
    }

    fun startScan() {
        if (_isScanning.value) return
        persistSettings()
        val ips = if (useManualIps.value) {
            currentScanSource = "Manual"
            IpGenerator.parseManualIps(manualIps.value)
        } else {
            currentScanSource = cidrInput.value
            IpGenerator.parseCidr(cidrInput.value)
        }
        if (ips.isEmpty()) {
            Toast.makeText(getApplication(),
                if (useManualIps.value) "Enter valid IPs" else "Invalid CIDR range", Toast.LENGTH_SHORT).show()
            return
        }
        val engine = ScannerEngine(timeout.value.toLongOrNull() ?: 3000, sni.value.ifBlank { "speed.cloudflare.com" })
        val sem = Semaphore(threads.value.toIntOrNull() ?: 30)
        _isScanning.value = true; _results.value = emptyList()
        _scanned.value = 0; _failed.value = 0; _total.value = ips.size; _progress.value = 0f; _startTime.value = System.currentTimeMillis()

        scanJob = viewModelScope.launch {
            ips.forEach { raw ->
                if (!_isScanning.value) return@forEach
                sem.withPermit {
                    launch {
                        val ip = IpGenerator.stripPort(raw)
                        val p = IpGenerator.extractPort(raw, port.value.toIntOrNull() ?: 443)
                        val result = engine.probe(ip, p)
                        if (result.isWorking) _results.value = _results.value + result
                        else _failed.value++
                        _scanned.value++
                        _progress.value = _scanned.value.toFloat() / ips.size
                    }
                }
            }
        }.also {
            it.invokeOnCompletion {
                _isScanning.value = false; _progress.value = 1f
                addScanToHistory()
                persistSettings()
            }
        }
    }

    private fun addScanToHistory() {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        _scanHistory.value = _scanHistory.value + ScanHistoryEntry(
            id = historyIdCounter++,
            timestamp = ts,
            totalIps = _total.value,
            workingCount = _results.value.size,
            failedCount = _failed.value,
            source = currentScanSource
        )
    }

    fun loadHistoryResult(entry: ScanHistoryEntry) {
        Toast.makeText(getApplication(), "Results from $entry.timestamp: ${entry.workingCount} working", Toast.LENGTH_SHORT).show()
    }

    fun clearHistory() { _scanHistory.value = emptyList() }

    fun stopScan() { _isScanning.value = false; scanJob?.cancel() }

    private fun sfx(): String = if (suffixOn.value) suffix.value else ""

    fun filteredResults(): List<ProbeResult> {
        val sorted = when (sortBy.value) {
            "grade" -> _results.value.sortedBy { it.grade.ordinal }
            "colo" -> _results.value.sortedBy { it.colo }
            else -> _results.value.sortedBy { it.tcpLatencyMs }
        }
        val filtered = sorted.filter { r ->
            val q = resultsSearch.value.lowercase()
            q.isBlank() || r.ip.contains(q) || r.colo.lowercase().contains(q) || r.grade.display.lowercase().contains(q)
        }
        val minGrade = filterGradeMin.value
        if (minGrade.isBlank()) return filtered
        val minOrdinal = Grade.entries.firstOrNull { it.display == minGrade }?.ordinal ?: return filtered
        return filtered.filter { it.grade.ordinal <= minOrdinal }
    }

    fun copyAll(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(filteredResults(), sfx())) }
    fun copyTop10(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.topN(filteredResults(), 10), sfx())) }
    fun packGreens(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.greens(filteredResults()), sfx())) }
    fun exportTxt(c: Context) { ExportUtils.exportToFile(c, filteredResults(), sfx()) }
    fun shareResults(c: Context) {
        val text = ExportUtils.formatList(filteredResults(), sfx())
        val file = File(c.cacheDir, "nova_radar_share.txt")
        file.writeText(text)
        val uri = FileProvider.getUriForFile(c, "${c.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        c.startActivity(Intent.createChooser(intent, "Share Results"))
    }
    fun copyIp(c: Context, result: ProbeResult) {
        ExportUtils.copyToClipboard(c, ExportUtils.applySuffix(result.ip, result.port, sfx()))
    }
    fun toggleFavorite(ip: String, port: Int) {
        val key = "$ip:$port"
        val current = favoriteIds.value.toMutableSet()
        if (key in current) current.remove(key) else current.add(key)
        favoriteIds.value = current
    }
    fun isFavorite(ip: String, port: Int): Boolean = "$ip:$port" in favoriteIds.value
    fun clearResults() { _results.value = emptyList() }

    fun setSortBy(mode: String) { sortBy.value = mode; settings.sortBy = mode }

    // ── Worker deployment state ──
    private val _workerStatus = MutableStateFlow("")
    val workerStatus: StateFlow<String> = _workerStatus.asStateFlow()
    private val _workerUrl = MutableStateFlow("")
    val workerUrl: StateFlow<String> = _workerUrl.asStateFlow()
    private val _isDeploying = MutableStateFlow(false)
    val isDeploying: StateFlow<Boolean> = _isDeploying.asStateFlow()
    private val _deploySteps = MutableStateFlow<List<DeployStep>>(emptyList())
    val deploySteps: StateFlow<List<DeployStep>> = _deploySteps.asStateFlow()

    data class DeployStep(val key: String, val labelEn: String, val labelFa: String, var state: String = "waiting")

    private val WORKER_JS_URL = "https://raw.githubusercontent.com/IRNova/Nova-Proxy/refs/heads/main/worker.js"

    private fun stepList(): List<DeployStep> = listOf(
        DeployStep("verify", "Verifying token", "بررسی توکن"),
        DeployStep("account", "Finding your account", "پیدا کردن حساب"),
        DeployStep("sub", "Setting up subdomain", "تنظیم زیردامنه"),
        DeployStep("d1", "Creating database", "ساخت دیتابیس"),
        DeployStep("kv", "Creating storage", "ساخت حافظه"),
        DeployStep("fetch", "Downloading latest Nova", "دانلود آخرین نسخه نوا"),
        DeployStep("deploy", "Deploying worker", "دیپلوی ورکر"),
        DeployStep("enable", "Turning it on", "روشن کردن"),
        DeployStep("online", "Waiting for it to come online", "منتظر آنلاین شدن"),
    )

    private fun setStep(key: String, state: String, statusText: String = "") {
        _deploySteps.value = _deploySteps.value.map { if (it.key == key) it.copy(state = state) else it }
        if (state == "running") _workerStatus.value = statusText.ifBlank {
            val s = _deploySteps.value.find { it.key == key } ?: return
            if (Strings.isRtl) s.labelFa else s.labelEn
        }
    }

    data class DeployResult(val url: String, val workerName: String)

    suspend fun deployWorker(token: String, isDemo: Boolean): DeployResult? {
        if (_isDeploying.value) return null
        _isDeploying.value = true; _workerStatus.value = ""; _workerUrl.value = ""
        _deploySteps.value = stepList()
        val isFa = Strings.isRtl

        if (isDemo) {
            for (s in _deploySteps.value) { setStep(s.key, "running"); kotlinx.coroutines.delay(500); setStep(s.key, "done") }
            val demoUrl = "https://nova-${kotlin.random.Random.nextInt(1000,9999)}.nova-demo.workers.dev"
            _workerUrl.value = demoUrl
            _isDeploying.value = false
            return DeployResult(demoUrl, "nova-demo")
        }

        val result = withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                fun cfApi(method: String, path: String, body: String? = null): Triple<Int, org.json.JSONObject?, String> {
                    val req = okhttp3.Request.Builder()
                        .url("https://api.cloudflare.com/client/v4$path")
                        .header("Authorization", "Bearer $token")
                    if (body != null) req.method(method, body.toRequestBody("application/json".toMediaType()))
                    else req.method(method, null)
                    val resp = client.newCall(req.build()).execute()
                    val txt = resp.body?.string() ?: ""
                    val json = try { org.json.JSONObject(txt) } catch (_: Exception) { null }
                    return Triple(resp.code, json, txt)
                }

                // 1. Verify token
                setStep("verify", "running")
                val (vCode, vJson, _) = cfApi("GET", "/user/tokens/verify")
                if (vCode != 200 || vJson == null || !vJson.optBoolean("success", false)) {
                    setStep("verify", "error"); return@withContext null
                }
                setStep("verify", "done")

                // 2. Get account ID
                setStep("account", "running")
                val (aCode, aJson, _) = cfApi("GET", "/accounts?per_page=50")
                if (aCode != 200 || aJson == null || !aJson.optBoolean("success", false)) {
                    setStep("account", "error"); return@withContext null
                }
                val accounts = aJson.optJSONArray("result")
                if (accounts == null || accounts.length() == 0) { setStep("account", "error"); return@withContext null }
                val accId = accounts.getJSONObject(0).optString("id", "")
                if (accId.isEmpty()) { setStep("account", "error"); return@withContext null }
                setStep("account", "done")

                // 3. Subdomain - check if exists
                setStep("sub", "running")
                var subName = ""
                val (sgCode, sgJson, _) = cfApi("GET", "/accounts/$accId/workers/subdomain")
                if (sgCode == 200 && sgJson?.optBoolean("success", false) == true) {
                    subName = sgJson.optJSONObject("result")?.optString("subdomain", "") ?: ""
                }
                if (subName.isEmpty()) {
                    val want = "nova-${kotlin.random.Random.nextInt(100000,999999)}"
                    val (spCode, spJson, _) = cfApi("PUT", "/accounts/$accId/workers/subdomain",
                        org.json.JSONObject().put("subdomain", want).toString())
                    if (spCode == 200 && spJson?.optBoolean("success", false) == true) {
                        subName = spJson.optJSONObject("result")?.optString("subdomain", "") ?: want
                    } else { subName = want }
                }
                setStep("sub", "done")

                // 4. Create D1 database
                setStep("d1", "running")
                val dbName = "nova-${kotlin.random.Random.nextInt(100000,999999)}-db"
                val (dbCode, dbJson, _) = cfApi("POST", "/accounts/$accId/d1/database",
                    org.json.JSONObject().put("name", dbName).toString())
                val dbId = if (dbCode == 200 && dbJson?.optBoolean("success", false) == true)
                    dbJson.optJSONObject("result")?.optString("uuid", "") ?: "" else ""
                if (dbId.isEmpty()) { setStep("d1", "error"); return@withContext null }
                setStep("d1", "done")

                // 5. Create KV namespace
                setStep("kv", "running")
                val kvName = "nova-${kotlin.random.Random.nextInt(100000,999999)}-kv"
                var kvId = ""
                try {
                    val (kvCode, kvJson, _) = cfApi("POST", "/accounts/$accId/storage/kv/namespaces",
                        org.json.JSONObject().put("title", kvName).toString())
                    if (kvCode == 200 && kvJson?.optBoolean("success", false) == true)
                        kvId = kvJson.optJSONObject("result")?.optString("id", "") ?: ""
                } catch (_: Exception) {}
                setStep("kv", "done")

                // 6. Fetch worker.js from GitHub
                setStep("fetch", "running")
                val ghReq = okhttp3.Request.Builder().url(WORKER_JS_URL)
                    .header("User-Agent", "Mozilla/5.0").build()
                val ghResp = client.newCall(ghReq).execute()
                if (!ghResp.isSuccessful) { setStep("fetch", "error"); return@withContext null }
                val workerCode = ghResp.body?.string() ?: ""
                if (workerCode.length < 1000 || !workerCode.contains("export default")) {
                    setStep("fetch", "error"); return@withContext null
                }
                setStep("fetch", "done")

                // 7. Deploy worker with multipart form-data
                setStep("deploy", "running")
                val workerName = "nova-${listOf("sunny","swift","atlas","orbit","pixel","falcon","crystal","mango","coral","luna","pearl","turbo","river","comet").random()}-${listOf("panel","bridge","node","core","wave","gate","stack","vault","portal","cloud","garden","spark").random()}-${kotlin.random.Random.nextInt(1000,9999)}"

                val bindsJson = org.json.JSONArray()
                if (kvId.isNotEmpty()) bindsJson.put(org.json.JSONObject().put("type", "kv_namespace").put("name", "KV").put("namespace_id", kvId))
                bindsJson.put(org.json.JSONObject().put("type", "d1").put("name", "DB").put("id", dbId))

                val metadata = org.json.JSONObject().apply {
                    put("main_module", "worker.js")
                    put("compatibility_date", "2024-09-23")
                    put("compatibility_flags", org.json.JSONArray(listOf("nodejs_compat")))
                    put("bindings", bindsJson)
                }

                val boundary = "----nova${kotlin.random.Random.nextInt(100000,999999)}"
                val bodyBuilder = StringBuilder()
                bodyBuilder.append("--${boundary}\r\n")
                bodyBuilder.append("Content-Disposition: form-data; name=\"metadata\"\r\n")
                bodyBuilder.append("Content-Type: application/json\r\n\r\n")
                bodyBuilder.append(metadata.toString()).append("\r\n")
                bodyBuilder.append("--${boundary}\r\n")
                bodyBuilder.append("Content-Disposition: form-data; name=\"worker.js\"; filename=\"worker.js\"\r\n")
                bodyBuilder.append("Content-Type: application/javascript+module\r\n\r\n")
                bodyBuilder.append(workerCode).append("\r\n")
                bodyBuilder.append("--${boundary}--\r\n")

                val depReq = okhttp3.Request.Builder()
                    .url("https://api.cloudflare.com/client/v4/accounts/$accId/workers/scripts/${java.net.URLEncoder.encode(workerName, "UTF-8")}")
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "multipart/form-data; boundary=$boundary")
                    .put(bodyBuilder.toString().toRequestBody(null))
                    .build()
                val depResp = client.newCall(depReq).execute()
                val depTxt = depResp.body?.string() ?: ""
                val depJson = try { org.json.JSONObject(depTxt) } catch (_: Exception) { null }
                if (!depResp.isSuccessful || depJson == null || !depJson.optBoolean("success", false)) {
                    setStep("deploy", "error"); return@withContext null
                }
                setStep("deploy", "done")

                // 8. Enable subdomain
                setStep("enable", "running")
                try {
                    cfApi("POST", "/accounts/$accId/workers/scripts/${java.net.URLEncoder.encode(workerName, "UTF-8")}/subdomain",
                        org.json.JSONObject().put("enabled", true).toString())
                } catch (_: Exception) {}
                setStep("enable", "done")

                // 9. Build URL
                val panelUrl = "https://$workerName.$subName.workers.dev"

                // 10. Wait for online
                setStep("online", "running")
                var online = false
                val deadline = System.currentTimeMillis() + 150000
                while (System.currentTimeMillis() < deadline) {
                    try {
                        val testResp = client.newCall(
                            okhttp3.Request.Builder().url("$panelUrl/install").header("Cache-Control", "no-store").build()
                        ).execute()
                        if (testResp.code in 200..499) { online = true; break }
                    } catch (_: Exception) {}
                    kotlinx.coroutines.delay(4000)
                }
                setStep("online", if (online) "done" else "done")

                _workerUrl.value = panelUrl
                _workerStatus.value = if (isFa) "✅ راه‌اندازی کامل شد!" else "✅ Deployed successfully!"
                DeployResult(panelUrl, workerName)
            } catch (e: Exception) {
                _workerStatus.value = if (isFa) "خطا: ${e.message}" else "Error: ${e.message}"
                null
            } finally {
                _isDeploying.value = false
            }
        }
        return result
    }

    fun generateVlessConfigs(results: List<ProbeResult>, uuid: String = "00000000-0000-0000-0000-000000000000", host: String = "speed.cloudflare.com"): String {
        return results.filter { it.isWorking && it.grade.ordinal <= Grade.B.ordinal }
            .sortedBy { it.tcpLatencyMs }
            .take(10)
            .joinToString("\n") { result ->
                val path = sfx().ifBlank { "/" }
                "vless://$uuid@${result.ip}:443?encryption=none&security=tls&sni=$host&type=ws&host=$host&path=${java.net.URLEncoder.encode(path, "UTF-8")}#Nova-${result.grade.display}-${result.colo}"
            }
    }
}
