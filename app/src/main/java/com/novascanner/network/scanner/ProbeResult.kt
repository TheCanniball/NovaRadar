package com.novascanner.network.scanner

data class ProbeResult(
    val ip: String,
    val port: Int,
    val tcpLatencyMs: Long,
    val tlsLatencyMs: Long,
    val httpLatencyMs: Long,
    val grade: Grade,
    val colo: String = ""
)

enum class Grade(val display: String, val threshold: Long) {
    S("S", 100),
    A("A", 200),
    B("B", 350),
    C("C", 500),
    D("D", 800),
    F("F", Long.MAX_VALUE);

    companion object {
        fun fromLatency(tcp: Long, tls: Long, http: Long): Grade {
            val avg = if (http > 0) (tcp + tls + http) / 3
                      else if (tls > 0) (tcp + tls) / 2
                      else tcp
            return entries.firstOrNull { avg <= it.threshold } ?: F
        }
    }
}
