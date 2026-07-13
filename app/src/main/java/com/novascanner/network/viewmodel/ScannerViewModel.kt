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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _results = MutableStateFlow<List<ProbeResult>>(emptyList())
    val results: StateFlow<List<ProbeResult>> = _results.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _scannedCount = MutableStateFlow(0)
    val scannedCount: StateFlow<Int> = _scannedCount.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    val manualIps = MutableStateFlow("")
    val cidrInput = MutableStateFlow("104.16.0.0/12")
    val scanCountInput = MutableStateFlow("50")
    val threadsInput = MutableStateFlow("30")
    val timeoutInput = MutableStateFlow("3000")
    val sniInput = MutableStateFlow("speed.cloudflare.com")
    val suffixInput = MutableStateFlow("?ed=2560")
    val suffixEnabled = MutableStateFlow(true)
    val portInput = MutableStateFlow("443")
    val inputMode = MutableStateFlow(true)

    private var scanJob: Job? = null

    fun startScan() {
        if (_isScanning.value) return

        val ips = if (inputMode.value) {
            IpGenerator.parseManualIp(manualIps.value)
        } else {
            IpGenerator.randomIpsFromCidr(cidrInput.value, scanCountInput.value.toIntOrNull() ?: 50)
        }

        if (ips.isEmpty()) {
            Toast.makeText(getApplication(), "No valid IPs!", Toast.LENGTH_SHORT).show()
            return
        }

        val port = portInput.value.toIntOrNull() ?: 443
        val threads = threadsInput.value.toIntOrNull() ?: 30
        val timeout = timeoutInput.value.toLongOrNull() ?: 3000
        val sni = sniInput.value.ifBlank { "speed.cloudflare.com" }

        val engine = ScannerEngine(threads = threads, timeoutMs = timeout, sniHost = sni)
        val semaphore = Semaphore(threads)

        _isScanning.value = true
        _results.value = emptyList()
        _scannedCount.value = 0
        _totalCount.value = ips.size
        _progress.value = 0f

        scanJob = viewModelScope.launch {
            ips.forEach { ip ->
                if (!_isScanning.value) return@forEach
                semaphore.withPermit {
                    launch {
                        val result = engine.probe(ip, port)
                        if (result != null) {
                            _results.value = _results.value + result
                        }
                        _scannedCount.value = _scannedCount.value + 1
                        _progress.value = _scannedCount.value.toFloat() / ips.size
                    }
                }
            }
        }.also { job ->
            job.invokeOnCompletion {
                _isScanning.value = false
                _progress.value = 1f
            }
        }
    }

    fun stopScan() {
        _isScanning.value = false
        scanJob?.cancel()
    }

    fun copyAll(context: Context) {
        val suffix = if (suffixEnabled.value) suffixInput.value else ""
        val text = ExportUtils.formatForClipboard(_results.value, suffix)
        ExportUtils.copyToClipboard(context, text)
    }

    fun copyTop10(context: Context) {
        val suffix = if (suffixEnabled.value) suffixInput.value else ""
        val top = ExportUtils.topResults(_results.value, 10)
        val text = ExportUtils.formatForClipboard(top, suffix)
        ExportUtils.copyToClipboard(context, text)
    }

    fun packGreens(context: Context) {
        val suffix = if (suffixEnabled.value) suffixInput.value else ""
        val greens = ExportUtils.greenResults(_results.value)
        val text = ExportUtils.formatForClipboard(greens, suffix)
        ExportUtils.copyToClipboard(context, text)
        val msg = "Copied ${greens.size} green IPs"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun exportTxt(context: Context) {
        val suffix = if (suffixEnabled.value) suffixInput.value else ""
        val path = ExportUtils.exportToFile(context, _results.value, suffix)
        if (path != null) {
            Toast.makeText(context, Strings.get("save_success"), Toast.LENGTH_LONG).show()
        }
    }

    fun clearResults() {
        _results.value = emptyList()
    }

    fun toggleLanguage() {
        Strings.isRtl = !Strings.isRtl
    }

    fun copySingle(context: Context, result: ProbeResult) {
        val suffix = if (suffixEnabled.value) suffixInput.value else ""
        val text = ExportUtils.applySuffix(result.ip, result.port, suffix)
        ExportUtils.copyToClipboard(context, text)
    }
}
