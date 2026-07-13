package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val ctx = LocalContext.current
    val sf = if (suffixOn) suffix else ""
    var showSortMenu by remember { mutableStateOf(false) }

    val displayResults = viewModel.filteredResults()
    val elapsed = if (isScanning && startTime > 0) "${(System.currentTimeMillis() - startTime) / 1000}s" else ""

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Nova Radar", color = Primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
            Text("${results.size} ${Strings.get("ips_found")}", color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))

        if (isScanning) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Primary, trackColor = SurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("$scanned / $total  $elapsed", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text("${Strings.get("failed")}: $failed", color = Error, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            NovaButton(Strings.get(if (isScanning) "stop_scan" else "start_scan"),
                onClick = { if (isScanning) viewModel.stopScan() else viewModel.startScan() },
                modifier = Modifier.weight(1f), color = if (isScanning) Error else Primary)
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Text("⇅", color = Primary, fontSize = 20.sp)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    listOf("latency", "grade", "colo").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.uppercase(), color = if (sortBy == mode) Primary else TextPrimary) },
                            onClick = { viewModel.setSortBy(mode); showSortMenu = false })
                    }
                }
            }
        }

        if (!isScanning) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (useManual) Strings.get("manual_ip") else Strings.get("cidr_scan"),
                    color = if (useManual) Primary else TextSecondary, fontSize = 13.sp,
                    modifier = Modifier.clickable { viewModel.useManualIps.value = true })
                Spacer(Modifier.width(4.dp))
                Switch(checked = useManual, onCheckedChange = { viewModel.useManualIps.value = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                Spacer(Modifier.width(4.dp))
                Text(if (!useManual) Strings.get("cidr_scan") else Strings.get("cidr_scan"),
                    color = if (!useManual) Primary else TextSecondary, fontSize = 13.sp,
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
            }
            if (!isScanning) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    NovaButton(Strings.get("copy_all"), onClick = { viewModel.copyAll(ctx) }, modifier = Modifier.weight(1f))
                    NovaButton(Strings.get("copy_top10"), onClick = { viewModel.copyTop10(ctx) }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    NovaButton(Strings.get("pack_greens"), onClick = { viewModel.packGreens(ctx) }, modifier = Modifier.weight(1f), color = Secondary)
                    NovaButton(Strings.get("export_txt"), onClick = { viewModel.exportTxt(ctx) }, modifier = Modifier.weight(1f))
                }
            }
        }

        if (displayResults.isEmpty() && !isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.get("no_results"), color = TextSecondary)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(displayResults) { result ->
                    ProbeCard(result, sf, onCopy = { viewModel.copyIp(ctx, result) })
                }
            }
        }
    }
}
