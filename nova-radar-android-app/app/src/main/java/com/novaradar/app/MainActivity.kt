package com.novaradar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novaradar.app.ui.localization.Localization
import com.novaradar.app.ui.screens.*
import com.novaradar.app.ui.theme.NovaRadarTheme
import com.novaradar.app.ui.viewmodel.AppLanguage
import com.novaradar.app.ui.viewmodel.AppTheme
import com.novaradar.app.ui.viewmodel.NovaRadarViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NovaRadarViewModel = viewModel()
            val theme by viewModel.selectedTheme.collectAsState()
            val lang by viewModel.selectedLanguage.collectAsState()

            NovaRadarTheme(theme = theme) {
                val view = androidx.compose.ui.platform.LocalView.current
                val isLightTheme = theme == AppTheme.PRISM_LIGHT
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as android.app.Activity).window
                        window.statusBarColor = android.graphics.Color.TRANSPARENT
                        window.navigationBarColor = android.graphics.Color.TRANSPARENT
                        val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
                        controller.isAppearanceLightStatusBars = isLightTheme
                        controller.isAppearanceLightNavigationBars = isLightTheme
                    }
                }

                val isDark = theme == AppTheme.PRISM_DARK
                val meshColors = if (isDark) {
                    listOf(Color(0xFF0A0E1A), Color(0xFF0F1A3A), Color(0xFF060A15))
                } else {
                    listOf(Color(0xFFFFFFFF), Color(0xFFEFF6FF), Color(0xFFF8FAFC))
                }
                val meshGradient = Brush.linearGradient(colors = meshColors)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(meshGradient)
                ) {
                    MainAppLayout(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: NovaRadarViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val theme by viewModel.selectedTheme.collectAsState()
    val isLightTheme = theme == AppTheme.PRISM_LIGHT
    val isDark = !isLightTheme
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Main content pager
            Box(Modifier.weight(1f)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_horizontal_pager")
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .padding(top = 8.dp)
                    ) {
                        when (page) {
                            0 -> EasyInstallerScreen(viewModel)
                            1 -> SettingsScreen(viewModel)
                            2 -> RadarScreen(viewModel)
                            3 -> ImportScreen(viewModel)
                            4 -> AboutScreen(viewModel)
                        }
                    }
                }
            }

            // Sticky bottom navigation bar
            val isScanning by viewModel.isScanning.collectAsState()
            val pulseAnim by rememberInfiniteTransition().animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                label = "navPulse"
            )
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(
                        width = 0.5.dp,
                        color = if (isDark) Color(0xFF2D3A5C).copy(alpha = 0.3f) else Color(0xFFCBD5E1).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ),
                containerColor = if (isDark) Color(0xFF0D1219).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                val items = listOf(
                    NavigationItemData(key = "tab_installer", selectedIcon = Icons.Filled.Download, unselectedIcon = Icons.Outlined.Download),
                    NavigationItemData(key = "tab_settings", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings),
                    NavigationItemData(key = "tab_radar", selectedIcon = Icons.Filled.Radar, unselectedIcon = Icons.Outlined.Radar),
                    NavigationItemData(key = "tab_import", selectedIcon = Icons.Filled.Add, unselectedIcon = Icons.Outlined.Add),
                    NavigationItemData(key = "tab_about", selectedIcon = Icons.Filled.Info, unselectedIcon = Icons.Outlined.Info)
                )

                items.forEachIndexed { index, item ->
                    val isSelected = pagerState.currentPage == index
                    val isRadar = index == 2

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRadar) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isScanning)
                                                    Brush.linearGradient(listOf(Color(0xFFBE123C), Color(0xFF9F1239)))
                                                else
                                                    Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
                                            )
                                            .clickable {
                                                if (isScanning) viewModel.stopScan()
                                                else { viewModel.startScan(); coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                                            }
                                            .testTag("nav_start_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, "Start/Stop", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = Localization.get(item.key, lang),
                                        tint = if (isSelected) {
                                            if (isDark) Color(0xFF4DA8FF) else Color(0xFF2563EB)
                                        } else {
                                            if (isDark) Color(0xFFE2E8F0).copy(alpha = 0.4f) else Color(0xFF334155).copy(alpha = 0.4f)
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                        modifier = Modifier.testTag("nav_item_${item.key}")
                    )
                }
            }
        }
    }
}

data class NavigationItemData(
    val key: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
