package com.novaradar.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaradar.app.ui.components.Wc
import com.novaradar.app.ui.components.WidgetCard
import com.novaradar.app.ui.localization.Localization
import com.novaradar.app.ui.viewmodel.AppTheme
import com.novaradar.app.ui.viewmodel.NovaRadarViewModel

@Composable
fun AboutScreen(viewModel: NovaRadarViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val theme by viewModel.selectedTheme.collectAsState()
    val isLight = theme == AppTheme.PRISM_LIGHT
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WidgetCard(isLightTheme = isLight) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = com.novaradar.app.R.drawable.img_nova_radar_logo_1781975654739),
                        contentDescription = "Logo",
                        modifier = Modifier.size(72.dp).clip(CircleShape).border(1.5.dp, Wc.primary.copy(alpha = 0.4f), CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("NOVA RADAR", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Wc.primary)
                    Text(Localization.get("about_sub", lang), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isLight) Color(0xFF4A5568) else Wc.onSurfaceVariantDark)
                    Spacer(Modifier.height(8.dp))
                    Text("v${com.novaradar.app.BuildConfig.VERSION_NAME}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                }
            }

            WidgetCard(isLightTheme = isLight) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("LINKS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Wc.primary.copy(alpha = 0.6f), letterSpacing = 1.sp)
                    AboutLink("GitHub", "https://github.com/IRNova/NovaRadar", Icons.Outlined.Code, isLight) { try { uriHandler.openUri("https://github.com/IRNova/NovaRadar") } catch (_: Exception) {} }
                    AboutLink("Telegram", "https://t.me/irnova_proxy", Icons.Outlined.Send, isLight) { try { uriHandler.openUri("https://t.me/irnova_proxy") } catch (_: Exception) {} }
                    AboutLink("Website", "https://novaproxy.online/install", Icons.Outlined.Language, isLight) { try { uriHandler.openUri("https://novaproxy.online/install") } catch (_: Exception) {} }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AboutLink(title: String, subtitle: String, icon: ImageVector, isLight: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isLight) Color(0xFFF1F5F9) else Wc.surfaceContainerDark.copy(alpha = 0.5f)).border(0.5.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Wc.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Wc.primary, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(12.dp))
        Column { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark); Text(subtitle, fontSize = 9.sp, color = if (isLight) Color(0xFF4A5568) else Wc.onSurfaceVariantDark) }
    }
}
