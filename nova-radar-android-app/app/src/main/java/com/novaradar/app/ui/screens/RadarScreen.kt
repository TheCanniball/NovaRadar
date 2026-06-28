package com.novaradar.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaradar.app.ui.components.StatWidget
import com.novaradar.app.ui.components.Wc
import com.novaradar.app.ui.components.WidgetCard
import com.novaradar.app.ui.localization.Localization
import com.novaradar.app.ui.viewmodel.AppLanguage
import com.novaradar.app.ui.viewmodel.AppTheme
import com.novaradar.app.ui.viewmodel.AliveIp
import com.novaradar.app.ui.viewmodel.NovaRadarViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val tlsPorts = setOf(443, 2053, 2083, 2087, 2096, 8443)

@Composable
fun RadarScreen(viewModel: NovaRadarViewModel) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsState()
    val theme by viewModel.selectedTheme.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scannedCount by viewModel.scannedCount.collectAsState()
    val aliveCount by viewModel.aliveCount.collectAsState()
    val deadCount by viewModel.deadCount.collectAsState()
    val eta by viewModel.etaValue.collectAsState()
    val subnetScanning by viewModel.currentScanningSubnet.collectAsState()
    val allIps by viewModel.allAliveIps.collectAsState()
    val recentProbes by viewModel.recentProbes.collectAsState()

    val isLight = theme == AppTheme.PRISM_LIGHT
    val subPagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var showConfigBuilder by remember { mutableStateOf(false) }
    var cfgUuid by remember { mutableStateOf("") }
    var cfgSni by remember { mutableStateOf("nova2.altramax083.workers.dev") }
    var cfgNetwork by remember { mutableStateOf("ws") }
    var cfgSecurity by remember { mutableStateOf("tls") }
    var cfgPath by remember { mutableStateOf("/") }

    val infiniteTransition = rememberInfiniteTransition(label = "sweep")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "angle"
    )

    val sweepBrush = remember {
        Brush.sweepGradient(
            0.0f to Color.Transparent, 0.5f to Color.Transparent,
            0.7f to Wc.primary.copy(alpha = 0.02f),
            0.85f to Wc.primary.copy(alpha = 0.20f),
            0.95f to Wc.primary.copy(alpha = 0.50f),
            1.0f to Wc.primary.copy(alpha = 0.85f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(bottom = 88.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Sub-tabs
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("tab_radar" to 0, "tab_results" to 1).forEach { (key, idx) ->
                    val active = subPagerState.currentPage == idx
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (active) Wc.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable(remember { MutableInteractionSource() }, null) { coroutineScope.launch { subPagerState.animateScrollToPage(idx) } }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                Localization.get(key, lang),
                                fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) Wc.primary else Color.Gray
                            )
                            if (idx == 1 && aliveCount > 0) {
                                Spacer(Modifier.width(6.dp))
                                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFEF4444)).padding(horizontal = 6.dp, vertical = 1.dp)) {
                                    Text("$aliveCount", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalPager(
                state = subPagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> ScannerTab(viewModel, isScanning, scannedCount, aliveCount, deadCount, eta, subnetScanning, allIps, recentProbes, animatedAngle, sweepBrush, context, lang, theme, isLight, showConfigBuilder, { showConfigBuilder = it }, cfgUuid, { cfgUuid = it }, cfgSni, { cfgSni = it }, cfgNetwork, { cfgNetwork = it }, cfgSecurity, { cfgSecurity = it }, cfgPath, { cfgPath = it })
                    1 -> ResultsTab(viewModel, isScanning, aliveCount, allIps, context, lang, isLight, showConfigBuilder, { showConfigBuilder = it }, cfgUuid, { cfgUuid = it }, cfgSni, { cfgSni = it }, cfgNetwork, { cfgNetwork = it }, cfgSecurity, { cfgSecurity = it }, cfgPath, { cfgPath = it })
                }
            }
        }
    }
}

@Composable
private fun ScannerTab(
    viewModel: NovaRadarViewModel, isScanning: Boolean, scannedCount: Int, aliveCount: Int, deadCount: Int,
    eta: String, subnetScanning: String, allIps: List<AliveIp>, recentProbes: List<String>,
    animatedAngle: Float, sweepBrush: Brush, context: android.content.Context,
    lang: AppLanguage, theme: AppTheme, isLight: Boolean,
    showConfigBuilder: Boolean, onShowConfigBuilder: (Boolean) -> Unit,
    cfgUuid: String, onCfgUuid: (String) -> Unit,
    cfgSni: String, onCfgSni: (String) -> Unit,
    cfgNetwork: String, onCfgNetwork: (String) -> Unit,
    cfgSecurity: String, onCfgSecurity: (String) -> Unit,
    cfgPath: String, onCfgPath: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Radar circle widget
        WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.12f)) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(CircleShape)
                    .background(if (isLight) Color(0xFFF0FDF4) else Color(0xFF021708))
                    .border(1.5.dp, Wc.primary.copy(alpha = if (isLight) 0.3f else 0.5f), CircleShape)
                    .clickable(remember { MutableInteractionSource() }, null) {
                        if (allIps.isNotEmpty()) {
                            viewModel.copyTop10ToClipboard(context)
                            Toast.makeText(context, Localization.get("copied_note", lang), Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val outer = r * 0.65f
                    drawCircle(color = Wc.primary.copy(alpha = 0.35f), radius = r * 0.35f, style = Stroke(1.5f))
                    drawCircle(color = Wc.primary.copy(alpha = 0.55f), radius = outer, style = Stroke(2.5f))
                    drawLine(color = Wc.primary.copy(alpha = 0.25f), start = Offset(c.x - outer, c.y), end = Offset(c.x + outer, c.y), strokeWidth = 1.5f)
                    drawLine(color = Wc.primary.copy(alpha = 0.25f), start = Offset(c.x, c.y - outer), end = Offset(c.x, c.y + outer), strokeWidth = 1.5f)
                    val sweepAlpha = if (isScanning) 1f else 0.2f
                    val aRad = Math.toRadians(animatedAngle.toDouble())
                    val ex = c.x + outer * cos(aRad).toFloat()
                    val ey = c.y + outer * sin(aRad).toFloat()
                    drawLine(color = Wc.primary.copy(alpha = 0.85f * sweepAlpha), start = c, end = Offset(ex, ey), strokeWidth = 3f)
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(c.x, c.y)
                        canvas.rotate(animatedAngle)
                        drawCircle(brush = sweepBrush, radius = outer, alpha = sweepAlpha)
                        canvas.restore()
                    }
                    drawCircle(color = Wc.primary, radius = 5.dp.toPx())
                    drawCircle(color = Color.Transparent, radius = 10.dp.toPx(), style = Stroke(1.5f.dp.toPx()))
                    allIps.take(8).forEachIndexed { _, alive ->
                        val daRad = Math.toRadians(alive.angle.toDouble())
                        val dp = alive.normalizedDistance * outer * 0.92f
                        val dx = c.x + dp * cos(daRad).toFloat()
                        val dy = c.y + dp * sin(daRad).toFloat()
                        val dotColor = when { alive.ping < 200 -> Wc.successLight; alive.ping < 500 -> Wc.warning; else -> Wc.error }
                        val angleDiff = (animatedAngle - alive.angle + 360f) % 360f
                        val pa = if (isScanning) (0.15f).coerceAtLeast(1f - (angleDiff / 240f)) else 0.2f
                        drawCircle(color = dotColor.copy(alpha = 0.35f * pa), radius = 6.dp.toPx(), center = Offset(dx, dy))
                        drawCircle(color = dotColor.copy(alpha = pa), radius = 3.dp.toPx(), center = Offset(dx, dy))
                    }
                }
            }
        }

        // Status bar
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(if (isLight) Color.White.copy(alpha = 0.5f) else Color(0xFF0D1219).copy(alpha = 0.5f))
                .border(0.5.dp, Wc.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(if (isScanning) Wc.success else Wc.success.copy(alpha = 0.3f)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isScanning) subnetScanning.ifEmpty { "SCANNING..." } else "STANDBY",
                        fontFamily = FontFamily.Monospace, fontSize = 9.sp,
                        color = (if (isScanning) Wc.success else Wc.successLight).copy(alpha = 0.8f)
                    )
                }
                Text("ETA $eta", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Wc.warning.copy(alpha = 0.7f))
            }
        }

        // 2x2 stat widgets
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatWidget(Modifier.weight(1f), "SCANNED", "$scannedCount", Wc.primary, Wc.primary, isLight)
            StatWidget(Modifier.weight(1f), "ALIVE", "$aliveCount", Wc.success, Wc.success, isLight)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatWidget(Modifier.weight(1f), "DEAD", "$deadCount", Wc.error, Wc.error, isLight)
            StatWidget(Modifier.weight(1f), "ETA", eta, Wc.warning, Wc.warning, isLight)
        }

        // Probe feed
        WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.08f)) {
            Text("PROBE FEED", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.5f), letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            if (recentProbes.isEmpty()) {
                Text("awaiting scan...", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.2f))
            } else {
                recentProbes.take(6).forEach { entry ->
                    Text(entry, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = (if (isLight) Color(0xFF1A202C) else Wc.success).copy(alpha = 0.6f), maxLines = 1)
                }
            }
        }

        // Clean IPs found
        if (allIps.isNotEmpty()) {
            WidgetCard(isLightTheme = isLight, borderColor = Wc.success.copy(alpha = 0.12f)) {
                Text("CLEAN FOUND", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.success.copy(alpha = 0.6f), letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                allIps.take(3).forEach { alive ->
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Wc.success.copy(alpha = 0.08f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("${alive.ip}:${alive.port}  ${alive.ping}ms", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.successLight.copy(alpha = 0.9f))
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultsTab(
    viewModel: NovaRadarViewModel, isScanning: Boolean, aliveCount: Int, allIps: List<AliveIp>,
    context: android.content.Context, lang: AppLanguage, isLight: Boolean,
    showConfigBuilder: Boolean, onShowConfigBuilder: (Boolean) -> Unit,
    cfgUuid: String, onCfgUuid: (String) -> Unit,
    cfgSni: String, onCfgSni: (String) -> Unit,
    cfgNetwork: String, onCfgNetwork: (String) -> Unit,
    cfgSecurity: String, onCfgSecurity: (String) -> Unit,
    cfgPath: String, onCfgPath: (String) -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Actions bar
        WidgetCard(isLightTheme = isLight) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape)
                        .background(if (isScanning) Wc.warning else if (allIps.isNotEmpty()) Wc.success else Wc.error.copy(alpha = 0.4f)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isScanning) "SCANNING..." else if (allIps.isNotEmpty()) "${allIps.size} TARGETS" else "NO TARGETS",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                        color = if (isScanning) Wc.warning else if (allIps.isNotEmpty()) Wc.success else Wc.error.copy(alpha = 0.5f)
                    )
                    if (allIps.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text("Ø${allIps.map { it.ping }.average().toLong()}ms", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.6f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(
                        Icons.Default.FormatListNumbered to { viewModel.copyTop10ToClipboard(context); Toast.makeText(context, Localization.get("copied_note", lang), Toast.LENGTH_SHORT).show() },
                        Icons.Default.ContentCopy to { viewModel.copyAllToClipboard(context) },
                        Icons.Default.Save to { viewModel.exportResultsToTxtFile(context) },
                        Icons.Default.Build to { onShowConfigBuilder(true) }
                    ).forEach { (icon, action) ->
                        IconButton(onClick = action, modifier = Modifier.size(24.dp)) {
                            Icon(icon, null, tint = Wc.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Export bar
        if (allIps.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Clash" to { viewModel.exportClash(context) }, "V2Ray" to { viewModel.exportV2Ray(context) },
                    "VLESS" to { viewModel.exportVLESS(context) }, "Sing-Box" to { viewModel.exportSingBox(context) }).forEach { (label, action) ->
                    Box(Modifier.weight(1f).height(26.dp).clip(RoundedCornerShape(6.dp))
                        .background(Wc.success.copy(alpha = 0.12f))
                        .border(0.5.dp, Wc.success.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .clickable(remember { MutableInteractionSource() }, null, onClick = action),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Wc.success)
                    }
                }
            }
        }

        // Results table
        if (allIps.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isScanning) "AWAITING TARGETS" else "NO TARGETS",
                        fontSize = 13.sp, fontFamily = FontFamily.Monospace,
                        color = (if (isScanning) Wc.warning else Wc.error).copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    Text(if (isScanning) "IPs appear as verified" else "Start a scan",
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray.copy(alpha = 0.5f))
                }
            }
        } else {
            Column(Modifier.weight(1f).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (isLight) Color(0xFFEDF2F7) else Color(0xFF1C2333))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("#", Modifier.width(20.dp), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("IP", Modifier.weight(1f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("PING", Modifier.width(36.dp), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("HTTP", Modifier.width(32.dp), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("SPD", Modifier.width(32.dp), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(allIps, key = { _, ip -> "${ip.ip}:${ip.port}" }) { index, alive ->
                        val speedKey = "${alive.ip}:${alive.port}"
                        val speedStr = viewModel.speedResults.value[speedKey] ?: "--"
                        val pingColor = when { alive.ping < 200 -> Wc.success; alive.ping < 500 -> Wc.warning; else -> Wc.error }
                        val httpColor = when { alive.httpPing < 0 -> Color.Gray.copy(alpha = 0.3f); alive.httpPing < 300 -> Wc.success; alive.httpPing < 600 -> Wc.warning; else -> Wc.error }

                        Box(Modifier.fillMaxWidth().background(if (index % 2 == 0) Color.Transparent else Color.White.copy(alpha = 0.02f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", Modifier.width(20.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                                Text(alive.ip, Modifier.weight(1f), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF1A202C) else Color(0xFFE8ECF4), maxLines = 1)
                                Text("${alive.ping}", Modifier.width(36.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = pingColor)
                                Text(if (alive.httpPing > 0) "${alive.httpPing}" else "--", Modifier.width(32.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = httpColor)
                                Row(Modifier.width(32.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(speedStr, fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Wc.warning.copy(alpha = 0.8f))
                                }
                            }
                            // Latency bar
                            Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 8.dp).height(1.dp)) {
                                val bw = (1f - (alive.ping.coerceAtMost(1000).toFloat() / 1000f)).coerceIn(0.02f, 0.98f)
                                Box(Modifier.weight(bw).fillMaxHeight().background(pingColor.copy(alpha = 0.3f)))
                                Box(Modifier.weight(1f - bw).fillMaxHeight().background(Color.Gray.copy(alpha = 0.06f)))
                            }
                            Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(":${alive.port}", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.4f))
                                if (alive.port in tlsPorts) {
                                    Box(Modifier.clip(RoundedCornerShape(2.dp)).background(Wc.primary.copy(alpha = 0.08f)).padding(horizontal = 3.dp)) {
                                        Text("TLS", fontSize = 5.sp, color = Wc.primary.copy(alpha = 0.5f))
                                    }
                                }
                                Box(Modifier.clip(RoundedCornerShape(2.dp)).background(Wc.primary.copy(alpha = 0.08f)).padding(horizontal = 3.dp)) {
                                    Text("Nova-${alive.novaId}", fontSize = 5.sp, color = Wc.primary.copy(alpha = 0.4f))
                                }
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(3.dp)).background(Wc.info.copy(alpha = 0.15f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.runSpeedTest(alive.ip, alive.port) }, contentAlignment = Alignment.Center) {
                                    Text("⚡", fontSize = 7.sp)
                                }
                                Box(Modifier.size(18.dp).clip(RoundedCornerShape(3.dp)).background(Wc.success.copy(alpha = 0.15f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.copyIndividualToClipboard(context, alive) }, contentAlignment = Alignment.Center) {
                                    Text("cpy", fontSize = 6.sp, color = Wc.success)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
