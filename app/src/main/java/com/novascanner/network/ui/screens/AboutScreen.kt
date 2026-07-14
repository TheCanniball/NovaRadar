package com.novascanner.network.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.theme.*
import com.novascanner.network.utils.Ob

@Composable
fun AboutScreen() {
    val ctx = LocalContext.current
    val b = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize().background(b.background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("tab_about"), color = b.primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get("app_desc"), color = b.onSurface, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text(Strings.get("build_info"), color = b.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Features", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "5-tab navigation (Radar, Import, Settings, Installer, About)",
                    "TCP + TLS + HTTP probe scanner engine",
                    "Grade system (SS/S/A/B/C/D/F)",
                    "CIDR scan with presets (Cloudflare, Fastly, Akamai)",
                    "Nova Proxy suffix system",
                    "Light/dark theme toggle",
                    "Retry count & delay between probes",
                    "Ping-only (TCP) mode",
                    "Auto-save results & scan notification",
                    "Animated progress & Material 3 UI",
                    "Real Nova Proxy Worker deploy (D1 + KV + subdomain)",
                    "VLESS config generation from scanned IPs",
                    "Search, sort & filter results",
                    "File import (SAF) & Share intent",
                    "Favorite star & expandable result cards",
                    "Scan history with stats",
                    "Settings persistence & scan profiles",
                    "Bilingual English/Persian with RTL support"
                ).forEach { feat ->
                    Text("- $feat", color = b.onSurfaceVariant, fontSize = 12.sp, lineHeight = 20.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Changelog", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("v1.1.0", color = b.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("• Light/dark theme toggle (dynamic theme)", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Retry count & delay between probes", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Ping-only (TCP) mode", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Auto-save results to file on scan complete", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Notification on scan complete", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Animated progress bar & smooth transitions", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Material 3 dynamic theme colors", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Scan engine retry & delay support", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• ABI split APKs (universal + per-arch)", color = b.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("v1.0.0", color = b.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("• Real Nova Proxy worker deploy (D1+KV+subdomain)", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• VLESS config generation from scan results", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Settings persistence via SharedPreferences", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Search, sort & filter results", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Favorite/star results + expandable cards", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• File import via SAF document picker", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Share results via Android intent", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• CIDR presets, scan history, failed counter", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Auto-copy best results on scan complete", color = b.onSurfaceVariant, fontSize = 12.sp)
                Text("• Save/load scan profiles", color = b.onSurfaceVariant, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        val gh = Ob.s("Eg4OCglAVVUdEw4SDxhUGRUXVS4SHzkbFBQTGBsWFlU0FQwbKBseGwg")
        NovaButton("Source Code", onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(gh)))
        }, modifier = Modifier.fillMaxWidth(), color = b.surfaceVariant)

        Spacer(Modifier.height(8.dp))

        NovaButton("Nova Proxy Official", onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Ob.s("Eg4OCglAVVUdEw4SDxhUGRUXVTMoNBUMG1U0FQwbVyoIFQID"))))
        }, modifier = Modifier.fillMaxWidth(), color = b.surfaceVariant)

        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Downloads", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text("Download the latest APK for your device:", color = b.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                val releasesUrl = "https://github.com/TheCanniball/NovaRadar/releases/tag/v1.1.0"
                val apks = listOf(
                    "Universal" to "NovaRadar-v1.1.0-universal.apk",
                    "arm64-v8a" to "NovaRadar-v1.1.0-arm64-v8a.apk",
                    "armeabi-v7a" to "NovaRadar-v1.1.0-armeabi-v7a.apk",
                    "x86_64" to "NovaRadar-v1.1.0-x86_64.apk",
                    "x86" to "NovaRadar-v1.1.0-x86.apk"
                )
                apks.forEachIndexed { i, (label, file) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${i + 1}.", color = b.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.width(20.dp))
                        Text(label, color = b.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.width(90.dp))
                        Text(file, color = b.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                NovaButton("Download from GitHub", onClick = {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releasesUrl)))
                }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Privacy", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("This app does not collect, store, or share any personal data. All scanning and configuration is performed locally on your device. No data is sent to any server except the Cloudflare API endpoints you explicitly authorize via your own token. Your token is used only on-device and never transmitted elsewhere.", color = b.onSurfaceVariant, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = b.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Licenses", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("OkHttp - Apache 2.0", color = b.onSurfaceVariant, fontSize = 11.sp)
                Text("Jetpack Compose - Apache 2.0", color = b.onSurfaceVariant, fontSize = 11.sp)
                Text("Kotlin Coroutines - Apache 2.0", color = b.onSurfaceVariant, fontSize = 11.sp)
                Text("Nova Proxy Worker - MIT", color = b.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}
