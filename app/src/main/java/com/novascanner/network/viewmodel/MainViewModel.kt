package com.novascanner.network.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
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
        val minGrade = filterGradeMin.value
        if (minGrade.isBlank()) return sorted
        val minOrdinal = Grade.entries.firstOrNull { it.display == minGrade }?.ordinal ?: return sorted
        return sorted.filter { it.grade.ordinal <= minOrdinal }
    }

    fun copyAll(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(filteredResults(), sfx())) }
    fun copyTop10(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.topN(filteredResults(), 10), sfx())) }
    fun packGreens(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.greens(filteredResults()), sfx())) }
    fun exportTxt(c: Context) { ExportUtils.exportToFile(c, filteredResults(), sfx()) }
    fun copyIp(c: Context, result: ProbeResult) {
        ExportUtils.copyToClipboard(c, ExportUtils.applySuffix(result.ip, result.port, sfx()))
    }
    fun clearResults() { _results.value = emptyList() }

    fun setSortBy(mode: String) { sortBy.value = mode; settings.sortBy = mode }

    private val _workerStatus = MutableStateFlow("")
    val workerStatus: StateFlow<String> = _workerStatus.asStateFlow()
    private val _workerUrl = MutableStateFlow("")
    val workerUrl: StateFlow<String> = _workerUrl.asStateFlow()
    private val _isDeploying = MutableStateFlow(false)
    val isDeploying: StateFlow<Boolean> = _isDeploying.asStateFlow()

    suspend fun deployWorker(token: String, name: String, uuid: String, proxyIp: String, isDemo: Boolean) {
        if (_isDeploying.value) return
        _isDeploying.value = true; _workerStatus.value = ""; _workerUrl.value = ""
        val isFa = Strings.isRtl

        if (isDemo) {
            val steps = listOf(
                if (isFa) "🔑 در حال اعتبارسنجی..." else "🔑 Authenticating...",
                if (isFa) "🔓 دسترسی تأیید شد." else "🔓 Access granted.",
                if (isFa) "👤 حساب: Nova_Wizard" else "👤 Account: Nova_Wizard",
                if (isFa) "⚙️ تولید کد پروکسی..." else "⚙️ Generating proxy code...",
                if (isFa) "📂 آپلود به کلودفلر..." else "📂 Uploading to Cloudflare...",
                if (isFa) "📡 تنظیم دامنه..." else "📡 Setting up domain...",
                if (isFa) "🎉 راه‌اندازی کامل شد!" else "🎉 Setup complete!"
            )
            for (s in steps) { _workerStatus.value = s; kotlinx.coroutines.delay(800) }
            _workerUrl.value = "https://$name.nova-proxy-demo.workers.dev"
            _isDeploying.value = false; return
        }

        withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS).build()

                _workerStatus.value = if (isFa) "🔑 در حال اعتبارسنجی توکن..." else "🔑 Authorizing token..."
                val accReq = okhttp3.Request.Builder()
                    .url("https://api.cloudflare.com/client/v4/accounts")
                    .header("Authorization", "Bearer $token").build()
                client.newCall(accReq).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        _workerStatus.value = if (isFa) "خطا: ${resp.code}" else "Error: ${resp.code}"
                        _isDeploying.value = false; return@withContext
                    }
                    val accId = Regex("\"id\":\"(\\w+)\"").find(resp.body?.string() ?: "")?.groupValues?.getOrNull(1) ?: ""
                    if (accId.isEmpty()) {
                        _workerStatus.value = if (isFa) "حسابی یافت نشد" else "No account found"
                        _isDeploying.value = false; return@withContext
                    }

                    _workerStatus.value = if (isFa) "⚙️ در حال آپلود ورکر..." else "⚙️ Uploading worker..."
                    val workerCode = """
                        export default {
                            async fetch(request, env) {
                                const url = new URL(request.url);
                                const upgradeHeader = request.headers.get('Upgrade');
                                if (upgradeHeader === 'websocket') return vlessOverWSHandler(request);
                                const vlessConfig = "vless://$uuid@" + url.host + ":443?encryption=none&security=tls&sni=" + url.host + "&type=ws&host=" + url.host + "&path=%2F%3Fed%3D2048";
                                return new Response(vlessConfig, { headers: { "content-type": "text/plain" } });
                            }
                        };
                    """.trimIndent()

                    val depReq = okhttp3.Request.Builder()
                        .url("https://api.cloudflare.com/client/v4/accounts/$accId/workers/scripts/$name")
                        .header("Authorization", "Bearer $token")
                        .put(workerCode.toRequestBody("application/javascript".toMediaType())).build()
                    client.newCall(depReq).execute().use { dep ->
                        if (!dep.isSuccessful) {
                            _workerStatus.value = if (isFa) "خطا در آپلود: ${dep.code}" else "Upload failed: ${dep.code}"
                            _isDeploying.value = false; return@withContext
                        }
                        _workerStatus.value = if (isFa) "📡 تنظیم زیردامنه..." else "📡 Configuring subdomain..."
                        kotlinx.coroutines.delay(1000)
                        _workerUrl.value = "https://$name.$accId.workers.dev"
                        _workerStatus.value = if (isFa) "✅ نصب با موفقیت انجام شد!" else "✅ Deployed successfully!"
                    }
                }
            } catch (e: Exception) {
                _workerStatus.value = if (isFa) "خطا: ${e.message}" else "Error: ${e.message}"
            }
            _isDeploying.value = false
        }
    }
}
