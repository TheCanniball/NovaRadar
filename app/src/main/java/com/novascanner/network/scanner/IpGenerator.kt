package com.novascanner.network.scanner

import kotlin.random.Random
import kotlin.math.pow

object IpGenerator {
    fun parseCidr(cidr: String, maxSamples: Int = 50): List<String> {
        try {
            val parts = cidr.trim().split("/")
            if (parts.size != 2) return emptyList()
            val ipParts = parts[0].split(".").map { it.toIntOrNull() ?: return emptyList() }
            if (ipParts.size != 4) return emptyList()
            val mask = parts[1].toIntOrNull() ?: return emptyList()
            if (mask < 8 || mask > 32) return emptyList()
            val base = ipParts.fold(0L) { acc, s -> (acc shl 8) + s }
            val hostBits = 32 - mask
            val maxHosts = (2.0.pow(hostBits)).toLong()
            val network = base shr hostBits shl hostBits
            val count = minOf(maxSamples, maxHosts.toInt() - 1).coerceAtLeast(1)
            val ips = mutableSetOf<String>()
            while (ips.size < count) {
                val offset = if (maxHosts <= 1) 0 else Random.nextLong(maxHosts - 1) + 1
                val ipLong = network + offset
                ips.add(longToIp(ipLong))
            }
            return ips.toList()
        } catch (_: Exception) { return emptyList() }
    }

    fun parseManualIps(input: String): List<String> {
        return input.lines()
            .flatMap { it.split(",", " ", "\t") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { it.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?$")) }
    }

    fun extractPort(ip: String, default: Int = 443): Int {
        val idx = ip.lastIndexOf(':')
        return if (idx > 0) ip.substring(idx + 1).toIntOrNull() ?: default else default
    }

    fun stripPort(ip: String): String {
        val idx = ip.lastIndexOf(':')
        return if (idx > 0) ip.substring(0, idx) else ip
    }

    private fun longToIp(value: Long): String = "${(value shr 24) and 0xFF}.${(value shr 16) and 0xFF}.${(value shr 8) and 0xFF}.${value and 0xFF}"
}
