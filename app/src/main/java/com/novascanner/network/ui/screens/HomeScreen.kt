package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaTextField
import com.novascanner.network.ui.components.ScanProgressBar
import com.novascanner.network.ui.components.SectionTitle
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.ScannerViewModel

@Composable
fun HomeScreen(
    viewModel: ScannerViewModel,
    onNavigateToResults: () -> Unit
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val scannedCount by viewModel.scannedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val resultCount by viewModel.results.collectAsState()
    val manualIps by viewModel.manualIps.collectAsState()
    val cidrInput by viewModel.cidrInput.collectAsState()
    val scanCount by viewModel.scanCountInput.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val port by viewModel.portInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Nova Radar",
            color = Primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = Strings.get("scanning"),
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = inputMode,
                onClick = { viewModel.inputMode.value = true },
                label = { Text(Strings.get("manual_ip")) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.2f),
                    selectedLabelColor = Primary
                )
            )
            FilterChip(
                selected = !inputMode,
                onClick = { viewModel.inputMode.value = false },
                label = { Text(Strings.get("cidr_range")) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.2f),
                    selectedLabelColor = Primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (inputMode) {
            SectionTitle(Strings.get("enter_ips"))
            NovaTextField(
                value = manualIps,
                onValueChange = { viewModel.manualIps.value = it },
                label = Strings.get("enter_ips"),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                singleLine = false,
                minLines = 3
            )
        } else {
            SectionTitle(Strings.get("enter_cidr"))
            NovaTextField(
                value = cidrInput,
                onValueChange = { viewModel.cidrInput.value = it },
                label = "CIDR",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            NovaTextField(
                value = scanCount,
                onValueChange = { viewModel.scanCountInput.value = it },
                label = Strings.get("scan_count"),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        NovaTextField(
            value = port,
            onValueChange = { viewModel.portInput.value = it },
            label = Strings.get("port"),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isScanning) {
            ScanProgressBar(progress = progress)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$scannedCount / $totalCount scanned | ${resultCount.size} results",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            NovaButton(
                text = Strings.get("stop_scan"),
                onClick = { viewModel.stopScan() },
                color = Error
            )
        } else {
            NovaButton(
                text = Strings.get("start_scan"),
                onClick = { viewModel.startScan() }
            )
        }

        if (resultCount.isNotEmpty() && !isScanning) {
            Spacer(modifier = Modifier.height(16.dp))
            NovaButton(
                text = "${Strings.get("results")} (${resultCount.size})",
                onClick = onNavigateToResults,
                color = Secondary
            )
        }
    }
}
