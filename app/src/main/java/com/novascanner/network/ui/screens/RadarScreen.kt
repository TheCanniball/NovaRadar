package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaField
import com.novascanner.network.ui.components.ProbeCard
import com.novascanner.network.ui.components.SectionTitle
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.MainViewModel

@Composable
fun RadarScreen(viewModel: MainViewModel) {
    val results by viewModel.results.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val scanned by viewModel.scanned.collectAsState()
    val total by viewModel.total.collectAsState()
    val suffix by viewModel.suffix.collectAsState()
    val suffixOn by viewModel.suffixOn.collectAsState()
    val ctx = LocalContext.current
    val sf = if (suffixOn) suffix else ""

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Nova Radar", color = Primary, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
            Text("${results.size} ${Strings.get("ips_found")}", color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))

        if (isScanning) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Primary, trackColor = SurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("$scanned / $total", color = TextSecondary, fontSize = 12.sp)
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            NovaButton(Strings.get(if (isScanning) "stop_scan" else "start_scan"),
                onClick = { if (isScanning) viewModel.stopScan() else viewModel.startScan() },
                modifier = Modifier.weight(1f), color = if (isScanning) Error else Primary)
        }

        if (results.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
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

        if (results.isEmpty() && !isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.get("no_results"), color = TextSecondary)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(results.sortedBy { it.tcpLatencyMs }) { result ->
                    ProbeCard(result, sf, onClick = { viewModel.copyAll(ctx) })
                }
            }
        }
    }
}
