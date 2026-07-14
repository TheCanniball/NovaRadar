package com.novascanner.network.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class AppSettings(context: Context) {
    private val p: SharedPreferences = context.getSharedPreferences("nova_radar", Context.MODE_PRIVATE)

    var port: String
        get() = p.getString("port", "443") ?: "443"
        set(v) { p.edit { putString("port", v) } }
    var threads: String
        get() = p.getString("threads", "30") ?: "30"
        set(v) { p.edit { putString("threads", v) } }
    var timeout: String
        get() = p.getString("timeout", "3000") ?: "3000"
        set(v) { p.edit { putString("timeout", v) } }
    var sni: String
        get() = p.getString("sni", "speed.cloudflare.com") ?: "speed.cloudflare.com"
        set(v) { p.edit { putString("sni", v) } }
    var suffix: String
        get() = p.getString("suffix", "?ed=2560") ?: "?ed=2560"
        set(v) { p.edit { putString("suffix", v) } }
    var suffixOn: Boolean
        get() = p.getBoolean("suffixOn", true)
        set(v) { p.edit { putBoolean("suffixOn", v) } }
    var isRtl: Boolean
        get() = p.getBoolean("isRtl", false)
        set(v) { p.edit { putBoolean("isRtl", v) } }
    var isDark: Boolean
        get() = p.getBoolean("isDark", true)
        set(v) { p.edit { putBoolean("isDark", v) } }
    var manualIps: String
        get() = p.getString("manualIps", "") ?: ""
        set(v) { p.edit { putString("manualIps", v) } }
    var cidr: String
        get() = p.getString("cidr", "104.16.0.0/12") ?: "104.16.0.0/12"
        set(v) { p.edit { putString("cidr", v) } }
    var sortBy: String
        get() = p.getString("sortBy", "latency") ?: "latency"
        set(v) { p.edit { putString("sortBy", v) } }
    var sampleSize: String
        get() = p.getString("sampleSize", "50") ?: "50"
        set(v) { p.edit { putString("sampleSize", v) } }
    var autoCopyBest: Boolean
        get() = p.getBoolean("autoCopyBest", false)
        set(v) { p.edit { putBoolean("autoCopyBest", v) } }

    var retryCount: Int
        get() = p.getInt("retryCount", 0)
        set(v) { p.edit { putInt("retryCount", v) } }
    var delayBetweenProbes: Int
        get() = p.getInt("delayBetweenProbes", 0)
        set(v) { p.edit { putInt("delayBetweenProbes", v) } }
    var pingOnly: Boolean
        get() = p.getBoolean("pingOnly", false)
        set(v) { p.edit { putBoolean("pingOnly", v) } }
    var autoSaveResults: Boolean
        get() = p.getBoolean("autoSaveResults", false)
        set(v) { p.edit { putBoolean("autoSaveResults", v) } }

    fun saveAll(port: String, threads: String, timeout: String, sni: String, suffix: String, suffixOn: Boolean, isRtl: Boolean, manualIps: String, cidr: String, sampleSize: String, autoCopyBest: Boolean, isDark: Boolean = true, retryCount: Int = 0, delayBetweenProbes: Int = 0, pingOnly: Boolean = false, autoSaveResults: Boolean = false) {
        p.edit {
            putString("port", port); putString("threads", threads); putString("timeout", timeout)
            putString("sni", sni); putString("suffix", suffix); putBoolean("suffixOn", suffixOn)
            putBoolean("isRtl", isRtl); putString("manualIps", manualIps); putString("cidr", cidr)
            putString("sampleSize", sampleSize); putBoolean("autoCopyBest", autoCopyBest)
            putBoolean("isDark", isDark); putInt("retryCount", retryCount)
            putInt("delayBetweenProbes", delayBetweenProbes); putBoolean("pingOnly", pingOnly)
            putBoolean("autoSaveResults", autoSaveResults)
        }
    }

    data class ScanProfile(val name: String, val cidr: String, val port: String, val sni: String, val threads: String, val timeout: String)

    fun loadProfiles(): List<ScanProfile> {
        val raw = p.getString("scanProfiles", "[]") ?: "[]"
        val arr = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            ScanProfile(o.optString("name", ""), o.optString("cidr", ""), o.optString("port", ""), o.optString("sni", ""), o.optString("threads", ""), o.optString("timeout", ""))
        }
    }

    fun saveProfiles(profiles: List<ScanProfile>) {
        val arr = JSONArray()
        profiles.forEach { arr.put(JSONObject().apply {
            put("name", it.name); put("cidr", it.cidr); put("port", it.port)
            put("sni", it.sni); put("threads", it.threads); put("timeout", it.timeout)
        }) }
        p.edit { putString("scanProfiles", arr.toString()) }
    }
}
