package com.novascanner.network.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

@Composable
fun AboutScreen() {
    val ctx = LocalContext.current

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("tab_about"), color = Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get("app_desc"), color = TextPrimary, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text(Strings.get("build_info"), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Features", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "5-tab navigation (Radar, Import, Settings, Installer, About)",
                    "TCP + TLS + HTTP probe scanner engine",
                    "Grade system (SS/S/A/B/C/D/F)",
                    "CIDR scan with presets (Cloudflare, Fastly, Akamai)",
                    "Nova Proxy suffix system",
                    "Real Nova Proxy Worker deploy (D1 + KV + subdomain)",
                    "VLESS config generation from scanned IPs",
                    "Search, sort & filter results",
                    "File import (SAF) & Share intent",
                    "Favorite star & expandable result cards",
                    "Scan history with stats",
                    "Settings persistence & scan profiles",
                    "Bilingual English/Persian with RTL support"
                ).forEach { feat ->
                    Text("- $feat", color = TextSecondary, fontSize = 12.sp, lineHeight = 20.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Changelog", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("v1.0.0", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("• Real Nova Proxy worker deploy (D1+KV+subdomain)", color = TextSecondary, fontSize = 12.sp)
                Text("• VLESS config generation from scan results", color = TextSecondary, fontSize = 12.sp)
                Text("• Settings persistence via SharedPreferences", color = TextSecondary, fontSize = 12.sp)
                Text("• Search, sort & filter results", color = TextSecondary, fontSize = 12.sp)
                Text("• Favorite/star results + expandable cards", color = TextSecondary, fontSize = 12.sp)
                Text("• File import via SAF document picker", color = TextSecondary, fontSize = 12.sp)
                Text("• Share results via Android intent", color = TextSecondary, fontSize = 12.sp)
                Text("• CIDR presets, scan history, failed counter", color = TextSecondary, fontSize = 12.sp)
                Text("• Auto-copy best results on scan complete", color = TextSecondary, fontSize = 12.sp)
                Text("• Save/load scan profiles", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        NovaButton("Source Code", onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheCanniball/NovaRadar")))
        }, modifier = Modifier.fillMaxWidth(), color = SurfaceVariant)

        Spacer(Modifier.height(8.dp))

        NovaButton("Nova Proxy Official", onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IRNova/Nova-Proxy")))
        }, modifier = Modifier.fillMaxWidth(), color = SurfaceVariant)

        Spacer(Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Downloads", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text("Download the latest APK for your device:", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                val releasesUrl = "https://github.com/TheCanniball/NovaRadar/releases/tag/v1.0.0"
                val apks = listOf(
                    "Universal" to "NovaRadar-v1.0.0-universal.apk",
                    "arm64-v8a" to "NovaRadar-v1.0.0-arm64-v8a.apk",
                    "armeabi-v7a" to "NovaRadar-v1.0.0-armeabi-v7a.apk",
                    "x86_64" to "NovaRadar-v1.0.0-x86_64.apk",
                    "x86" to "NovaRadar-v1.0.0-x86.apk"
                )
                apks.forEachIndexed { i, (label, file) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${i + 1}.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(20.dp))
                        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.width(90.dp))
                        Text(file, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                NovaButton("Download from GitHub", onClick = {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releasesUrl)))
                }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Licenses", color = Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("OkHttp - Apache 2.0", color = TextSecondary, fontSize = 11.sp)
                Text("Jetpack Compose - Apache 2.0", color = TextSecondary, fontSize = 11.sp)
                Text("Kotlin Coroutines - Apache 2.0", color = TextSecondary, fontSize = 11.sp)
                Text("Nova Proxy Worker - MIT", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}
