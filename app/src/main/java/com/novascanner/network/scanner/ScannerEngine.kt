package com.novascanner.network.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket

class ScannerEngine(
    private val timeoutMs: Long = 3000,
    private val sniHost: String = "speed.cloudflare.com"
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .build()

    fun pingOnly(ip: String, port: Int = 443, callback: (ProbeResult) -> Unit) {
        Thread {
            val start = System.currentTimeMillis()
            try {
                val socket = Socket().apply {
                    connect(InetSocketAddress(ip, port), timeoutMs.toInt())
                    soTimeout = timeoutMs.toInt()
                }
                val tcpTime = System.currentTimeMillis() - start
                socket.close()
                callback(ProbeResult(ip, port, tcpTime, 0, 0, Grade.fromLatency(tcpTime, 0, 0), isWorking = true))
            } catch (_: Exception) {
                callback(ProbeResult(ip, port, isWorking = false))
            }
        }.start()
    }

    suspend fun probe(ip: String, port: Int = 443): ProbeResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
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
                return@withContext ProbeResult(ip, port, tcpTime, 0, 0, Grade.F, isWorking = true)
            }

            try {
                sslSocket.sslParameters = SSLParameters().apply {
                    serverNames = listOf(SNIHostName(sniHost))
                }
                sslSocket.startHandshake()
                val tlsTime = System.currentTimeMillis() - tlsStart

                val httpStart = System.currentTimeMillis()
                val colo = try {
                    val req = Request.Builder().url("https://$ip/cdn-cgi/trace").header("Host", sniHost).build()
                    val resp = httpClient.newCall(req).execute()
                    val body = resp.body?.string() ?: ""
                    resp.close()
                    body.lines().firstOrNull { it.startsWith("colo=") }?.removePrefix("colo=") ?: ""
                } catch (_: Exception) { "" }
                val httpTime = System.currentTimeMillis() - httpStart

                val grade = Grade.fromLatency(tcpTime, tlsTime, httpTime)
                ProbeResult(ip, port, tcpTime, tlsTime, httpTime, grade, colo, isWorking = true)
            } finally { sslSocket.close() }
        } catch (_: Exception) {
            ProbeResult(ip, port, isWorking = false)
        }
    }
}
