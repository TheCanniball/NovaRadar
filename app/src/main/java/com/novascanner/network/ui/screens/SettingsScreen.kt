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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.components.NovaButton
import com.novascanner.network.ui.components.NovaTextField
import com.novascanner.network.ui.components.SectionTitle
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.ScannerViewModel

@Composable
fun SettingsScreen(viewModel: ScannerViewModel) {
    val threads by viewModel.threadsInput.collectAsState()
    val timeout by viewModel.timeoutInput.collectAsState()
    val sni by viewModel.sniInput.collectAsState()
    val suffix by viewModel.suffixInput.collectAsState()
    val suffixEnabled by viewModel.suffixEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = Strings.get("settings"),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(Strings.get("suffix_text"))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            NovaTextField(
                value = suffix,
                onValueChange = { viewModel.suffixInput.value = it },
                label = Strings.get("suffix_hint"),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = suffixEnabled,
                onCheckedChange = { viewModel.suffixEnabled.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle(Strings.get("threads"))
        NovaTextField(
            value = threads,
            onValueChange = { viewModel.threadsInput.value = it },
            label = Strings.get("threads"),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(Strings.get("timeout"))
        NovaTextField(
            value = timeout,
            onValueChange = { viewModel.timeoutInput.value = it },
            label = Strings.get("timeout"),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(Strings.get("sni_host"))
        NovaTextField(
            value = sni,
            onValueChange = { viewModel.sniInput.value = it },
            label = Strings.get("sni_host"),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(Strings.get("language"))
        NovaButton(
            text = if (Strings.isRtl) "English" else "فارسی",
            onClick = { viewModel.toggleLanguage() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("about"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Strings.get("app_desc"),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${Strings.get("version")}: 1.0.0",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = Strings.get("build_info"),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
