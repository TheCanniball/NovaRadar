package com.novascanner.network.scanner

data class ProbeResult(
    val ip: String,
    val port: Int,
    val tcpLatencyMs: Long = 0,
    val tlsLatencyMs: Long = 0,
    val httpLatencyMs: Long = 0,
    val grade: Grade = Grade.F,
    val colo: String = "",
    val isWorking: Boolean = false
)

enum class Grade(val display: String, val threshold: Long) {
    SS("SS", 80), S("S", 120), A("A", 200), B("B", 350), C("C", 500), D("D", 750), F("F", Long.MAX_VALUE);

    companion object {
        fun fromLatency(tcp: Long, tls: Long, http: Long): Grade {
            if (tcp == 0L && tls == 0L) return F
            val avg = when {
                http > 0 -> (tcp + tls + http) / 3
                tls > 0 -> (tcp + tls) / 2
                else -> tcp
            }
            return entries.firstOrNull { avg <= it.threshold } ?: F
        }
    }
}
