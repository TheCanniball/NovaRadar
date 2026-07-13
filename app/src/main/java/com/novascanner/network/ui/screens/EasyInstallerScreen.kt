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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaField
import com.novascanner.network.ui.theme.*
import com.novascanner.network.utils.ExportUtils
import com.novascanner.network.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun EasyInstallerScreen(viewModel: MainViewModel) {
    val workerStatus by viewModel.workerStatus.collectAsState()
    val workerUrl by viewModel.workerUrl.collectAsState()
    val isDeploying by viewModel.isDeploying.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiToken by remember { mutableStateOf("") }
    var workerName by remember { mutableStateOf("nova-proxy") }
    var uuid by remember { mutableStateOf("") }
    var proxyIp by remember { mutableStateOf("") }
    var isDemo by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("worker_title"), color = Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Strings.get("worker_demo"), color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = isDemo, onCheckedChange = { isDemo = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                }
                Spacer(Modifier.height(12.dp))

                if (!isDemo) {
                    NovaField(apiToken, { apiToken = it }, Strings.get("worker_api"), Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }

                NovaField(workerName, { workerName = it }, Strings.get("worker_name"), Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                if (!isDemo) {
                    NovaField(uuid, { uuid = it }, Strings.get("worker_uuid"), Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }

                NovaField(proxyIp, { proxyIp = it }, Strings.get("worker_ip"), Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(16.dp))

                NovaButton(Strings.get("worker_deploy"), onClick = {
                    scope.launch { viewModel.deployWorker(apiToken, workerName, uuid, proxyIp, isDemo) }
                }, enabled = !isDeploying, modifier = Modifier.fillMaxWidth())

                if (workerStatus.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(workerStatus, color = if (workerUrl.isNotBlank()) Secondary else TextPrimary, fontSize = 13.sp)
                }

                if (workerUrl.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(workerUrl, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    NovaButton(Strings.get("copy"), onClick = { ExportUtils.copyToClipboard(ctx, workerUrl) })
                }
            }
        }
    }
}
