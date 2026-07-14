package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaField
import com.novascanner.network.ui.components.SectionTitle
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val suffix by viewModel.suffix.collectAsState()
    val suffixOn by viewModel.suffixOn.collectAsState()
    val threads by viewModel.threads.collectAsState()
    val timeout by viewModel.timeout.collectAsState()
    val sni by viewModel.sni.collectAsState()
    val history by viewModel.scanHistory.collectAsState()
    val sampleSize by viewModel.sampleSize.collectAsState()
    val autoCopyBest by viewModel.autoCopyBest.collectAsState()
    val profiles by viewModel.scanProfiles.collectAsState()
    val retryCount by viewModel.retryCount.collectAsState()
    val delayBetweenProbes by viewModel.delayBetweenProbes.collectAsState()
    val pingOnly by viewModel.pingOnly.collectAsState()
    val autoSaveResults by viewModel.autoSaveResults.collectAsState()
    val isDark by viewModel.isDark.collectAsState()
    val b = MaterialTheme.colorScheme

    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProfiles() }

    Column(Modifier.fillMaxSize().background(b.background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("tab_settings"), color = b.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))

        SectionTitle("Scan Profiles")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            profiles.take(4).forEach { p ->
                Surface(color = b.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { viewModel.applyProfile(p) }) {
                    Text(p.name, color = b.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NovaButton("+ Save current", onClick = { showSaveDialog = true }, modifier = Modifier.weight(1f), color = b.surfaceVariant)
        }
        if (showSaveDialog) {
            AlertDialog(onDismissRequest = { showSaveDialog = false },
                containerColor = b.surface,
                title = { Text("Save Profile", color = b.onSurface) },
                text = {
                    Column {
                        Text("Name for this config:", color = b.onSurfaceVariant, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        NovaField(profileName, { profileName = it }, "e.g. Cloudflare Fast", Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = { if (profileName.isNotBlank()) { viewModel.saveProfile(profileName.trim()); profileName = ""; showSaveDialog = false } }) {
                        Text("Save", color = b.primary) }
                },
                dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = b.onSurfaceVariant) } })
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(Strings.get("suffix"))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NovaField(suffix, { viewModel.suffix.value = it }, Strings.get("suffix_hint"), Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Switch(checked = suffixOn, onCheckedChange = { viewModel.suffixOn.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(Strings.get("threads"))
        Spacer(Modifier.height(8.dp))
        NovaField(threads, { viewModel.threads.value = it }, "10-100", Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        SectionTitle(Strings.get("timeout"))
        Spacer(Modifier.height(8.dp))
        NovaField(timeout, { viewModel.timeout.value = it }, "1000-10000", Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        SectionTitle("Sample Size (CIDR)")
        Spacer(Modifier.height(8.dp))
        NovaField(sampleSize, { viewModel.sampleSize.value = it }, "10-500", Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        SectionTitle(Strings.get("sni"))
        Spacer(Modifier.height(8.dp))
        NovaField(sni, { viewModel.sni.value = it }, "speed.cloudflare.com", Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.get("retry"), color = b.onSurface, fontSize = 14.sp, modifier = Modifier.width(120.dp))
            Spacer(Modifier.width(8.dp))
            NovaField(retryCount, { viewModel.retryCount.value = it }, "0-5", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.get("delay"), color = b.onSurface, fontSize = 14.sp, modifier = Modifier.width(120.dp))
            Spacer(Modifier.width(8.dp))
            NovaField(delayBetweenProbes, { viewModel.delayBetweenProbes.value = it }, "0-1000", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.get("ping_only"), color = b.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(checked = pingOnly, onCheckedChange = { viewModel.pingOnly.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.get("auto_save"), color = b.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(checked = autoSaveResults, onCheckedChange = { viewModel.autoSaveResults.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-copy Top 10 on finish", color = b.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(checked = autoCopyBest, onCheckedChange = { viewModel.autoCopyBest.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.get("theme"), color = b.onSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(checked = isDark, onCheckedChange = { viewModel.toggleDark() },
                colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle(Strings.get("lang"))
        Spacer(Modifier.height(8.dp))
        NovaButton(text = if (Strings.isRtl) "English" else "فارسی", onClick = { viewModel.toggleLang() })

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle("Scan History")
            Spacer(Modifier.height(8.dp))
            history.reversed().take(10).forEach { entry ->
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = b.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Row(Modifier.padding(12.dp).fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("${entry.source}  •  ${entry.timestamp}", color = b.onSurfaceVariant, fontSize = 11.sp)
                            Text("${entry.workingCount} working / ${entry.failedCount} failed", color = b.onSurface, fontSize = 13.sp)
                        }
                        Text("${entry.totalIps}", color = b.primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            NovaButton("Clear History", onClick = { viewModel.clearHistory() }, color = Error)
        }
    }
}
