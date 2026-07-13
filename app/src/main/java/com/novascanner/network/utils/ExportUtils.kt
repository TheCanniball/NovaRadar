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

    fun copyToClipboard(context: Context, text: String, label: String = "IP") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    fun exportToFile(context: Context, results: List<ProbeResult>, suffix: String): String? {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(downloadsDir, "NovaRadar_$dateStr.txt")
            val content = results.joinToString("\n") {
                applySuffix(it.ip, it.port, suffix)
            }
            file.writeText(content)
            return file.absolutePath
        } catch (_: Exception) { return null }
    }

    fun formatForClipboard(results: List<ProbeResult>, suffix: String): String {
        return results.joinToString("\n") { applySuffix(it.ip, it.port, suffix) }
    }

    fun topResults(results: List<ProbeResult>, n: Int): List<ProbeResult> {
        return results.sortedBy { it.tcpLatencyMs }.take(n)
    }

    fun greenResults(results: List<ProbeResult>): List<ProbeResult> {
        return results.filter { it.grade.ordinal <= GradeOrdinal.C }
    }
}

private object GradeOrdinal { const val C = 3 }
