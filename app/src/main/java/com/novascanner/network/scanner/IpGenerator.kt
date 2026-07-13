package com.novascanner.network.scanner

import kotlin.random.Random
import kotlin.math.pow

object IpGenerator {

    data class CidrRange(val base: Long, val mask: Int)

    fun parseCidr(cidr: String): CidrRange? {
        try {
            val parts = cidr.split("/")
            if (parts.size != 2) return null
            val ipParts = parts[0].split(".")
            if (ipParts.size != 4) return null
            val mask = parts[1].toInt()
            if (mask < 8 || mask > 32) return null
            val base = ipParts.fold(0L) { acc, s -> (acc shl 8) + s.toLong() }
            return CidrRange(base, mask)
        } catch (_: Exception) { return null }
    }

    fun randomIpsFromCidr(cidr: String, count: Int): List<String> {
        val range = parseCidr(cidr) ?: return emptyList()
        val hostBits = 32 - range.mask
        val maxHosts = 2.0.pow(hostBits).toLong()
        val actualCount = minOf(count, maxHosts.toInt())
        val results = mutableSetOf<String>()
        val network = range.base shr hostBits shl hostBits
        while (results.size < actualCount) {
            val offset = if (maxHosts <= 1) 0 else Random.nextLong(maxHosts - 1) + 1
            val ipLong = network + offset
            results.add(longToIp(ipLong))
        }
        return results.toList()
    }

    fun parseManualIp(input: String): List<String> {
        return input.lines()
            .flatMap { it.split(",", " ", "\t") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { it.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?$")) }
    }

    private fun longToIp(value: Long): String {
        return "${(value shr 24) and 0xFF}.${(value shr 16) and 0xFF}.${(value shr 8) and 0xFF}.${value and 0xFF}"
    }
}
