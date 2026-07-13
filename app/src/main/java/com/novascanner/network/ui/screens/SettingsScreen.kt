package com.novascanner.network.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("tab_settings"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))

        SectionTitle(Strings.get("suffix"))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            NovaField(suffix, { viewModel.suffix.value = it }, Strings.get("suffix_hint"), Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Switch(checked = suffixOn, onCheckedChange = { viewModel.suffixOn.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Primary))
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
        SectionTitle(Strings.get("sni"))
        Spacer(Modifier.height(8.dp))
        NovaField(sni, { viewModel.sni.value = it }, "speed.cloudflare.com", Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))
        SectionTitle(Strings.get("lang"))
        Spacer(Modifier.height(8.dp))
        NovaButton(text = if (Strings.isRtl) "English" else "فارسی", onClick = { viewModel.toggleLang() })
    }
}
