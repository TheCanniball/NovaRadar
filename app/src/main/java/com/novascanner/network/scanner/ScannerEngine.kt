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
    private val threads: Int = 50,
    private val timeoutMs: Long = 3000,
    private val sniHost: String = "speed.cloudflare.com"
) {
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(2000, TimeUnit.MILLISECONDS)
            .writeTimeout(2000, TimeUnit.MILLISECONDS)
            .followRedirects(false)
            .build()
    }

    suspend fun probe(ip: String, port: Int = 443): ProbeResult? = withContext(Dispatchers.IO) {
        try {
            val (tcpTime, tlsTime, httpTime, colo) = probeAll(ip, port)
            val grade = Grade.fromLatency(tcpTime, tlsTime, httpTime)
            ProbeResult(ip, port, tcpTime, tlsTime, httpTime, grade, colo)
        } catch (_: Exception) { null }
    }

    private data class ProbeTimes(
        val tcp: Long = 0,
        val tls: Long = 0,
        val http: Long = 0,
        val colo: String = ""
    )

    private fun probeAll(ip: String, port: Int): ProbeTimes {
        val tcpStart = System.currentTimeMillis()
        val socket = Socket().apply {
            connect(InetSocketAddress(ip, port), timeoutMs.toInt())
            soTimeout = timeoutMs.toInt()
        }
        val tcpTime = System.currentTimeMillis() - tcpStart

        val sslSocket: SSLSocket
        val tlsStart: Long
        try {
            tlsStart = System.currentTimeMillis()
            val ctx = javax.net.ssl.SSLContext.getDefault()
            sslSocket = ctx.socketFactory.createSocket(socket, ip, port, true) as SSLSocket
        } catch (_: Exception) {
            socket.close()
            return ProbeTimes(tcp = tcpTime)
        }

        return try {
            val params = SSLParameters().apply {
                serverNames = listOf(SNIHostName(sniHost))
            }
            sslSocket.sslParameters = params
            sslSocket.startHandshake()
            val tlsTime = System.currentTimeMillis() - tlsStart

            val httpStart = System.currentTimeMillis()
            val colo = try {
                val request = Request.Builder()
                    .url("https://$ip/cdn-cgi/trace")
                    .header("Host", sniHost)
                    .build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                response.close()
                body.lines().firstOrNull { it.startsWith("colo=") }?.removePrefix("colo=") ?: ""
            } catch (_: Exception) { "" }
            val httpTime = System.currentTimeMillis() - httpStart

            ProbeTimes(tcp = tcpTime, tls = tlsTime, http = httpTime, colo = colo)
        } finally {
            sslSocket.close()
        }
    }
}
