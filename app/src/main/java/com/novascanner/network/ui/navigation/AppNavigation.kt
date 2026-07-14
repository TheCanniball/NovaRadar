package com.novascanner.network.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.screens.*
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.MainViewModel

data class Tab(val key: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val isCenter: Boolean = false)

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val tabs = remember {
        listOf(
            Tab("tab_settings", Icons.Filled.Settings, Icons.Outlined.Settings),
            Tab("tab_import", Icons.Filled.CloudDownload, Icons.Outlined.CloudDownload),
            Tab("tab_radar", Icons.Filled.Radar, Icons.Outlined.Radar, isCenter = true),
            Tab("tab_installer", Icons.Filled.Bolt, Icons.Outlined.Bolt),
            Tab("tab_about", Icons.Filled.Info, Icons.Outlined.Info)
        )
    }
    var current by remember { mutableIntStateOf(2) }
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")

    Scaffold(bottomBar = {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { i, tab ->
                    if (tab.isCenter) {
                        Spacer(Modifier.weight(1f))
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f))
                            .scale(if (current == i) scale else 1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { current = i },
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Radar, contentDescription = Strings.get(tab.key),
                                tint = if (current == i) Primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.weight(1f))
                    } else {
                        Column(modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { current = i },
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (current == i) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = Strings.get(tab.key),
                                tint = if (current == i) Primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            Text(Strings.get(tab.key), color = if (current == i) Primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when (current) { 0 -> SettingsScreen(viewModel); 1 -> ImportScreen(viewModel); 2 -> RadarScreen(viewModel); 3 -> EasyInstallerScreen(viewModel); 4 -> AboutScreen() }
        }
    }
}
