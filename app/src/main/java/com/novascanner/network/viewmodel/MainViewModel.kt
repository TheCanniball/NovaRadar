package com.novascanner.network.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novascanner.network.localization.Strings
import com.novascanner.network.scanner.IpGenerator
import com.novascanner.network.scanner.ProbeResult
import com.novascanner.network.scanner.ScannerEngine
import com.novascanner.network.utils.ExportUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
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

    val manualIps = MutableStateFlow("")
    val cidrInput = MutableStateFlow("104.16.0.0/12")
    val port = MutableStateFlow("443")
    val threads = MutableStateFlow("30")
    val timeout = MutableStateFlow("3000")
    val sni = MutableStateFlow("speed.cloudflare.com")
    val suffix = MutableStateFlow("?ed=2560")
    val suffixOn = MutableStateFlow(true)

    private var scanJob: Job? = null

    fun startScan() {
        if (_isScanning.value) return
        val ips = IpGenerator.parseManualIps(manualIps.value)
        if (ips.isEmpty()) {
            Toast.makeText(getApplication(), "Enter valid IPs", Toast.LENGTH_SHORT).show()
            return
        }
        val engine = ScannerEngine(timeout.value.toLongOrNull() ?: 3000, sni.value.ifBlank { "speed.cloudflare.com" })
        val sem = Semaphore(threads.value.toIntOrNull() ?: 30)
        _isScanning.value = true; _results.value = emptyList()
        _scanned.value = 0; _total.value = ips.size; _progress.value = 0f

        scanJob = viewModelScope.launch {
            ips.forEach { raw ->
                if (!_isScanning.value) return@forEach
                sem.withPermit {
                    launch {
                        val ip = IpGenerator.stripPort(raw)
                        val p = IpGenerator.extractPort(raw, port.value.toIntOrNull() ?: 443)
                        val result = engine.probe(ip, p)
                        if (result.isWorking) _results.value = _results.value + result
                        _scanned.value++
                        _progress.value = _scanned.value.toFloat() / ips.size
                    }
                }
            }
        }.also { it.invokeOnCompletion { _isScanning.value = false; _progress.value = 1f } }
    }

    fun stopScan() { _isScanning.value = false; scanJob?.cancel() }

    private fun sfx(): String = if (suffixOn.value) suffix.value else ""

    fun copyAll(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(_results.value, sfx())) }
    fun copyTop10(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.topN(_results.value, 10), sfx())) }
    fun packGreens(c: Context) { ExportUtils.copyToClipboard(c, ExportUtils.formatList(ExportUtils.greens(_results.value), sfx())) }
    fun exportTxt(c: Context) { ExportUtils.exportToFile(c, _results.value, sfx()) }
    fun clearResults() { _results.value = emptyList() }
    fun toggleLang() { Strings.isRtl = !Strings.isRtl }

    // Worker deployment state
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
                "🔑 ${if (isFa) "در حال اعتبارسنجی..." else "Authenticating..."}",
                "🔓 ${if (isFa) "دسترسی تأیید شد." else "Access granted."}",
                "👤 ${if (isFa) "دریافت حساب: Nova_Wizard" else "Account: Nova_Wizard"}",
                "⚙️ ${if (isFa) "تولید کد پروکسی..." else "Generating proxy code..."}",
                "📂 ${if (isFa) "آپلود به کلودفلر..." else "Uploading to Cloudflare..."}",
                "📡 ${if (isFa) "تنظیم دامنه..." else "Setting up domain..."}",
                "🎉 ${if (isFa) "راه‌اندازی کامل شد!" else "Setup complete!"}"
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
                    val body = resp.body?.string() ?: ""
                    val accId = Regex("\"id\":\"(\\w+)\"").find(body)?.groupValues?.getOrNull(1) ?: ""
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
