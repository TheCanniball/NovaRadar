package com.novascanner.network.utils

import android.util.Base64

object Ob {
    private const val K = 0x7A

    fun s(e: String): String {
        val d = Base64.decode(e, Base64.DEFAULT)
        return String(d.map { (it.toInt() xor K).toByte() }.toByteArray(), Charsets.UTF_8)
    }

    fun e(s: String): String {
        val x = s.toByteArray(Charsets.UTF_8).map { (it.toInt() xor K).toByte() }.toByteArray()
        return Base64.encodeToString(x, Base64.DEFAULT).trimEnd('=', '\n', '\r')
    }
}
