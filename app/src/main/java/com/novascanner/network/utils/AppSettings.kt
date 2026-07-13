package com.novascanner.network.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_radar", Context.MODE_PRIVATE)

    var port: String
        get() = prefs.getString("port", "443") ?: "443"
        set(v) { prefs.edit { putString("port", v) } }

    var threads: String
        get() = prefs.getString("threads", "30") ?: "30"
        set(v) { prefs.edit { putString("threads", v) } }

    var timeout: String
        get() = prefs.getString("timeout", "3000") ?: "3000"
        set(v) { prefs.edit { putString("timeout", v) } }

    var sni: String
        get() = prefs.getString("sni", "speed.cloudflare.com") ?: "speed.cloudflare.com"
        set(v) { prefs.edit { putString("sni", v) } }

    var suffix: String
        get() = prefs.getString("suffix", "?ed=2560") ?: "?ed=2560"
        set(v) { prefs.edit { putString("suffix", v) } }

    var suffixOn: Boolean
        get() = prefs.getBoolean("suffix_on", true)
        set(v) { prefs.edit { putBoolean("suffix_on", v) } }

    var isRtl: Boolean
        get() = prefs.getBoolean("is_rtl", false)
        set(v) { prefs.edit { putBoolean("is_rtl", v) } }

    var manualIps: String
        get() = prefs.getString("manual_ips", "") ?: ""
        set(v) { prefs.edit { putString("manual_ips", v) } }

    var cidr: String
        get() = prefs.getString("cidr", "104.16.0.0/12") ?: "104.16.0.0/12"
        set(v) { prefs.edit { putString("cidr", v) } }

    var sortBy: String
        get() = prefs.getString("sort_by", "latency") ?: "latency"
        set(v) { prefs.edit { putString("sort_by", v) } }

    var sampleSize: String
        get() = prefs.getString("sample_size", "50") ?: "50"
        set(v) { prefs.edit { putString("sample_size", v) } }

    var autoCopyBest: Boolean
        get() = prefs.getBoolean("auto_copy_best", false)
        set(v) { prefs.edit { putBoolean("auto_copy_best", v) } }

    fun saveAll(port: String, threads: String, timeout: String, sni: String,
                suffix: String, suffixOn: Boolean, isRtl: Boolean,
                manualIps: String, cidr: String, sampleSize: String, autoCopyBest: Boolean) {
        prefs.edit {
            putString("port", port); putString("threads", threads)
            putString("timeout", timeout); putString("sni", sni)
            putString("suffix", suffix); putBoolean("suffix_on", suffixOn)
            putBoolean("is_rtl", isRtl); putString("manual_ips", manualIps)
            putString("cidr", cidr); putString("sample_size", sampleSize)
            putBoolean("auto_copy_best", autoCopyBest)
        }
    }

    // ── Scan profiles ──
    data class ScanProfile(val name: String, val cidr: String, val port: String, val sni: String, val threads: String, val timeout: String)

    fun loadProfiles(): List<ScanProfile> {
        val raw = prefs.getString("scan_profiles", "[]") ?: "[]"
        val arr = try { JSONArray(raw) } catch (_: Exception) { return emptyList() }
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            ScanProfile(o.optString("name", ""), o.optString("cidr", ""), o.optString("port", "443"),
                o.optString("sni", "speed.cloudflare.com"), o.optString("threads", "30"), o.optString("timeout", "3000"))
        }.filter { it.name.isNotBlank() }
    }

    fun saveProfiles(profiles: List<ScanProfile>) {
        val arr = JSONArray()
        profiles.forEach { p ->
            arr.put(JSONObject().apply {
                put("name", p.name); put("cidr", p.cidr); put("port", p.port)
                put("sni", p.sni); put("threads", p.threads); put("timeout", p.timeout)
            })
        }
        prefs.edit { putString("scan_profiles", arr.toString()) }
    }
}
