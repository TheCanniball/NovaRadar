package com.novascanner.network.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.novascanner.network.scanner.ProbeResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    fun applySuffix(ip: String, port: Int, suffix: String): String {
        return if (suffix.isBlank()) "$ip:$port"
        else if (suffix.startsWith("?")) "$ip:$port$suffix"
        else if (suffix.startsWith("/")) "$ip:$port$suffix"
        else "$ip:$port?$suffix"
    }

    fun copyToClipboard(context: Context, text: String) {
        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clip.setPrimaryClip(ClipData.newPlainText("NovaRadar", text))
        Toast.makeText(context, "Copied ${text.lines().size} IPs", Toast.LENGTH_SHORT).show()
    }

    fun exportToFile(context: Context, results: List<ProbeResult>, suffix: String): String? {
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val name = "NovaRadar_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"
            val file = File(dir, name)
            file.writeText(results.joinToString("\n") { applySuffix(it.ip, it.port, suffix) })
            Toast.makeText(context, "Saved: $name", Toast.LENGTH_LONG).show()
            return file.path
        } catch (_: Exception) { return null }
    }

    fun formatList(results: List<ProbeResult>, suffix: String): String =
        results.joinToString("\n") { applySuffix(it.ip, it.port, suffix) }

    fun topN(results: List<ProbeResult>, n: Int): List<ProbeResult> =
        results.filter { it.isWorking }.sortedBy { it.tcpLatencyMs }.take(n)

    fun greens(results: List<ProbeResult>): List<ProbeResult> =
        results.filter { it.isWorking && it.grade.ordinal <= GradeOrdinal.C }
}

private object GradeOrdinal { const val C = 3 }
