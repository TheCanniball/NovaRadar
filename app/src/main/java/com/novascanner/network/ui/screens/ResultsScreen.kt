package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.novascanner.network.ui.components.ResultCard
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.ScannerViewModel

@Composable
fun ResultsScreen(
    viewModel: ScannerViewModel,
    onBack: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val suffixEnabled by viewModel.suffixEnabled.collectAsState()
    val suffixInput by viewModel.suffixInput.collectAsState()
    val suffix = if (suffixEnabled) suffixInput else ""
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("results"),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${results.size} ${Strings.get("ips_found")}",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NovaButton(
                text = Strings.get("copy_all"),
                onClick = { viewModel.copyAll(context) },
                modifier = Modifier.weight(1f)
            )
            NovaButton(
                text = Strings.get("copy_top10"),
                onClick = { viewModel.copyTop10(context) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NovaButton(
                text = Strings.get("pack_greens"),
                onClick = { viewModel.packGreens(context) },
                modifier = Modifier.weight(1f),
                color = Secondary
            )
            NovaButton(
                text = Strings.get("export_txt"),
                onClick = { viewModel.exportTxt(context) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Strings.get("suffix_text"),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = suffixEnabled,
                    onCheckedChange = { viewModel.suffixEnabled.value = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }
            TextButton(onClick = {
                viewModel.clearResults()
                onBack()
            }) {
                Text(Strings.get("clear_results"), color = Error, fontSize = 12.sp)
            }
        }

        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.get("no_results"),
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results.sortedBy { it.tcpLatencyMs }) { result ->
                    ResultCard(
                        result = result,
                        suffix = suffix,
                        onCopy = { viewModel.copySingle(context, result) }
                    )
                }
            }
        }
    }
}
