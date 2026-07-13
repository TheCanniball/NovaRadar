package com.novascanner.network.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

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

    fun saveAll(port: String, threads: String, timeout: String, sni: String,
                suffix: String, suffixOn: Boolean, isRtl: Boolean,
                manualIps: String, cidr: String) {
        prefs.edit {
            putString("port", port)
            putString("threads", threads)
            putString("timeout", timeout)
            putString("sni", sni)
            putString("suffix", suffix)
            putBoolean("suffix_on", suffixOn)
            putBoolean("is_rtl", isRtl)
            putString("manual_ips", manualIps)
            putString("cidr", cidr)
        }
    }
}
