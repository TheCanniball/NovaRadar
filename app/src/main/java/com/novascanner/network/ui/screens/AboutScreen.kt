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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.theme.*

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().background(Background).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Strings.get("tab_about"), color = Primary, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get("app_desc"), color = TextPrimary, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text(Strings.get("build_info"), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Nova Radar", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("- " + Strings.get("tab_radar"), color = TextSecondary, fontSize = 13.sp)
                Text("- " + Strings.get("tab_import"), color = TextSecondary, fontSize = 13.sp)
                Text("- " + Strings.get("tab_installer"), color = TextSecondary, fontSize = 13.sp)
                Text("- " + Strings.get("tab_settings"), color = TextSecondary, fontSize = 13.sp)
                Text("- " + Strings.get("tab_about"), color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
