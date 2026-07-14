package com.novascanner.network.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.scanner.Grade
import com.novascanner.network.ui.components.*
import com.novascanner.network.ui.theme.*
import com.novascanner.network.utils.Ob
import com.novascanner.network.viewmodel.MainViewModel

@Composable
fun RadarScreen(viewModel: MainViewModel) {
    val results by viewModel.results.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val scanned by viewModel.scanned.collectAsState()
    val total by viewModel.total.collectAsState()
    val failed by viewModel.failed.collectAsState()
    val suffix by viewModel.suffix.collectAsState()
    val suffixOn by viewModel.suffixOn.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val useManual by viewModel.useManualIps.collectAsState()
    val cidr by viewModel.cidrInput.collectAsState()
    val manualIps by viewModel.manualIps.collectAsState()
    val filterGrade by viewModel.filterGradeMin.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val searchQuery by viewModel.resultsSearch.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val ctx = LocalContext.current
    val b = MaterialTheme.colorScheme
    val sf = if (suffixOn) suffix else ""
    var showSortMenu by remember { mutableStateOf(false) }

    val displayResults = viewModel.filteredResults()
    val elapsed = if (isScanning && startTime > 0) "${(System.currentTimeMillis() - startTime) / 1000}s" else ""
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(300))

    Column(Modifier.fillMaxSize().background(b.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Nova Radar", color = b.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
            Text("${results.size} ${Strings.get("ips_found")}", color = b.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))

        if (isScanning) {
            LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = b.primary, trackColor = b.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("$scanned / $total  $elapsed", color = b.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text("${Strings.get("failed")}: $failed", color = Error, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            NovaButton(Strings.get(if (isScanning) "stop_scan" else "start_scan"),
                onClick = { if (isScanning) viewModel.stopScan() else viewModel.startScan() },
                modifier = Modifier.weight(1f), color = if (isScanning) Error else b.primary)
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Text("⇅", color = b.primary, fontSize = 20.sp)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    listOf("latency", "grade", "colo").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.uppercase(), color = if (sortBy == mode) b.primary else b.onSurface) },
                            onClick = { viewModel.setSortBy(mode); showSortMenu = false })
                    }
                }
            }
        }

        if (!isScanning) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (useManual) Strings.get("manual_ip") else Strings.get("manual_ip"),
                    color = if (useManual) b.primary else b.onSurfaceVariant, fontSize = 13.sp,
                    modifier = Modifier.clickable { viewModel.useManualIps.value = true })
                Spacer(Modifier.width(4.dp))
                Switch(checked = useManual, onCheckedChange = { viewModel.useManualIps.value = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = b.primary))
                Spacer(Modifier.width(4.dp))
                Text(if (!useManual) Strings.get("cidr_scan") else Strings.get("cidr_scan"),
                    color = if (!useManual) b.primary else b.onSurfaceVariant, fontSize = 13.sp,
                    modifier = Modifier.clickable { viewModel.useManualIps.value = false })
            }
            Spacer(Modifier.height(6.dp))

            if (!useManual) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Presets.all.forEach { preset ->
                        PresetChip(preset.name, cidr == preset.cidr) {
                            viewModel.cidrInput.value = preset.cidr
                            viewModel.sni.value = preset.sni
                            viewModel.port.value = preset.port
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                NovaField(cidr, { viewModel.cidrInput.value = it }, Strings.get("enter_cidr"), Modifier.fillMaxWidth())
            } else {
                NovaField(manualIps, { viewModel.manualIps.value = it },
                    Strings.get("enter_ips"), Modifier.fillMaxWidth().heightIn(min = 100.dp), singleLine = false, lines = 3)
            }
        }

        if (results.isNotEmpty()) {
            if (!isScanning) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Grade.entries.take(5).forEach { g ->
                        val selected = filterGrade == g.display
                        PresetChip(g.display, selected) {
                            viewModel.filterGradeMin.value = if (selected) "" else g.display
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                NovaField(searchQuery, { viewModel.resultsSearch.value = it }, "Search IP / Colo / Grade",
                    Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(6.dp))
            }
            if (!isScanning) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    NovaButton(Strings.get("pack_greens"), onClick = { viewModel.packGreens(ctx) }, modifier = Modifier.weight(1f), color = Secondary)
                    NovaButton("Share", onClick = { viewModel.shareResults(ctx) }, modifier = Modifier.weight(1f), color = b.primary)
                }
                Spacer(Modifier.height(6.dp))
                NovaButton(Ob.s("PR8UHwgbDh9aLDY/KSlaORUUHBMdWlIuFQpaS0pT"),
                    onClick = {
                        val vless = viewModel.generateVlessConfigs(viewModel.filteredResults())
                        val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clip.setPrimaryClip(android.content.ClipData.newPlainText(Ob.s("LDY/KSk"), vless))
                    },
                    modifier = Modifier.fillMaxWidth(), color = Secondary)
            }
        }

        if (displayResults.isEmpty() && !isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.get("no_results"), color = b.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(displayResults) { result ->
                    ProbeCard(result, sf, onCopy = { viewModel.copyIp(ctx, result) },
                        isFavorite = viewModel.isFavorite(result.ip, result.port),
                        onToggleFavorite = { viewModel.toggleFavorite(result.ip, result.port) })
                }
            }
        }
    }
}
