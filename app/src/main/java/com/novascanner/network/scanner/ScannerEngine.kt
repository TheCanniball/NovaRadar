package com.novascanner.network.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.novascanner.network.utils.Ob
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket

class ScannerEngine(
    private val timeoutMs: Long = 3000,
    private val sniHost: String = Ob.s("CQofHx5UGRYVDx4cFhsIH1QZFRc"),
    private val retryCount: Int = 0,
    private val delayMs: Long = 0
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .build()

    fun pingOnly(ip: String, port: Int = 443, callback: (ProbeResult) -> Unit) {
        Thread {
            val result = tryProbe(ip, port, false)
            callback(result)
        }.start()
    }

    suspend fun probe(ip: String, port: Int = 443): ProbeResult = withContext(Dispatchers.IO) {
        tryProbe(ip, port, true)
    }

    private fun tryProbe(ip: String, port: Int, full: Boolean): ProbeResult {
        var lastError: Exception? = null
        val attempts = retryCount + 1
        for (i in 0 until attempts) {
            try {
                if (i > 0) Thread.sleep(delayMs)
                return if (full) fullProbe(ip, port) else pingProbe(ip, port)
            } catch (e: Exception) {
                lastError = e
            }
        }
        return ProbeResult(ip, port, isWorking = false)
    }

    private fun pingProbe(ip: String, port: Int): ProbeResult {
        val start = System.currentTimeMillis()
        val socket = Socket().apply {
            connect(InetSocketAddress(ip, port), timeoutMs.toInt())
            soTimeout = timeoutMs.toInt()
        }
        val tcpTime = System.currentTimeMillis() - start
        socket.close()
        return ProbeResult(ip, port, tcpTime, 0, 0, Grade.fromLatency(tcpTime, 0, 0), isWorking = true)
    }

    private fun fullProbe(ip: String, port: Int): ProbeResult {
        val start = System.currentTimeMillis()
        val socket = Socket().apply {
            connect(InetSocketAddress(ip, port), timeoutMs.toInt())
            soTimeout = timeoutMs.toInt()
        }
        val tcpTime = System.currentTimeMillis() - start
        val tlsStart = System.currentTimeMillis()
        val sslSocket = try {
            val ctx = javax.net.ssl.SSLContext.getDefault()
            ctx.socketFactory.createSocket(socket, ip, port, true) as SSLSocket
        } catch (_: Exception) {
            socket.close()
            return ProbeResult(ip, port, tcpTime, 0, 0, Grade.F, isWorking = true)
        }
        try {
            sslSocket.sslParameters = SSLParameters().apply {
                serverNames = listOf(SNIHostName(sniHost))
            }
            sslSocket.startHandshake()
            val tlsTime = System.currentTimeMillis() - tlsStart

            val httpStart = System.currentTimeMillis()
            val colo = try {
                val req = Request.Builder().url("${Ob.s("Eg4OCglAVVU=")}$ip${Ob.s("VRkeFFcZHRNVDggbGR8")}").header(Ob.s("MhUJDg"), sniHost).build()
                val resp = httpClient.newCall(req).execute()
                val body = resp.body?.string() ?: ""
                resp.close()
                body.lines().firstOrNull { it.startsWith(Ob.s("GRUWFUc")) }?.removePrefix(Ob.s("GRUWFUc")) ?: ""
            } catch (_: Exception) { "" }
            val httpTime = System.currentTimeMillis() - httpStart

            val grade = Grade.fromLatency(tcpTime, tlsTime, httpTime)
            return ProbeResult(ip, port, tcpTime, tlsTime, httpTime, grade, colo, isWorking = true)
        } finally { sslSocket.close() }
    }
}
