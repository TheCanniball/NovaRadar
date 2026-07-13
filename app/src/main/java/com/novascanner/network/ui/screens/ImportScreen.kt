package com.novascanner.network.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaField
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.MainViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ImportScreen(viewModel: MainViewModel) {
    val manualIps by viewModel.manualIps.collectAsState()
    val port by viewModel.port.collectAsState()
    val ctx = LocalContext.current
    var importStatus by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val reader = BufferedReader(InputStreamReader(ctx.contentResolver.openInputStream(uri)))
            val lines = reader.readText()
            reader.close()
            val existing = viewModel.manualIps.value.trim()
            viewModel.manualIps.value = if (existing.isBlank()) lines.trim() else "$existing\n${lines.trim()}"
            importStatus = "Imported ${lines.lines().size} lines"
        } catch (e: Exception) {
            importStatus = "Error: ${e.message}"
        }
    }

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("manual_ip"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get("enter_ips"), color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                NovaField(value = manualIps, onValueChange = { viewModel.manualIps.value = it },
                    label = "1.2.3.4:443\n5.6.7.8:443", modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    singleLine = false, lines = 4)
                Spacer(Modifier.height(12.dp))
                NovaField(value = port, onValueChange = { viewModel.port.value = it },
                    label = Strings.get("port"), modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get("tab_import"), color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                NovaButton("Import from file (.txt)", onClick = { filePicker.launch(arrayOf("text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth())
                if (importStatus.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(importStatus, color = Secondary, fontSize = 13.sp)
                }
            }
        }
    }
}
