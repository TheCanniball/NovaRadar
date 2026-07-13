package com.novascanner.network.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.novascanner.network.localization.Strings
import com.novascanner.network.ui.screens.HomeScreen
import com.novascanner.network.ui.screens.ResultsScreen
import com.novascanner.network.ui.screens.SettingsScreen
import com.novascanner.network.ui.theme.*
import com.novascanner.network.viewmodel.ScannerViewModel

enum class Screen(
    val labelKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Scan("scan", Icons.Filled.Radar, Icons.Outlined.Radar),
    Results("results", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    Settings("settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppNavigation(viewModel: ScannerViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Scan) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == screen) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = Strings.get(screen.labelKey)
                            )
                        },
                        label = { Text(Strings.get(screen.labelKey)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                Screen.Scan -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToResults = { currentScreen = Screen.Results }
                )
                Screen.Results -> ResultsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.Scan }
                )
                Screen.Settings -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
