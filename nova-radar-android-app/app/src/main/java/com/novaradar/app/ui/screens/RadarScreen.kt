package com.novaradar.app.ui.screens

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.novaradar.app.ui.components.ParticleBackground
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
import kotlin.random.Random

private val tlsPorts = setOf(443, 2053, 2083, 2087, 2096, 8443)

private enum class Rank(val label: String, val maxPing: Int, val color: Color) {
    S("S", 80, Color(0xFF10B981)),
    A("A", 200, Color(0xFF3B82F6)),
    B("B", 400, Color(0xFFF59E0B)),
    C("C", Int.MAX_VALUE, Color(0xFFEF4444))
}

private fun pingRank(ping: Long): Rank = when {
    ping < 80 -> Rank.S
    ping < 200 -> Rank.A
    ping < 400 -> Rank.B
    else -> Rank.C
}

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

    val detectedNetwork = remember(context) { viewModel.getNetworkType(context) }
    val networkOverride by viewModel.networkTypeOverride.collectAsState()
    val networkType = if (networkOverride.isNotEmpty()) networkOverride else detectedNetwork
    val desktopMode by viewModel.desktopMode.collectAsState()
    var selectedOperator by remember { mutableStateOf("all") }
    val coroutineScope = rememberCoroutineScope()
    var showConfigBuilder by remember { mutableStateOf(false) }
    var showFullscreenRadar by remember { mutableStateOf(false) }
    val cfgOutput by viewModel.cfgOutput.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "sweep")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "angle"
    )

    val sweepBrush = remember {
        Brush.sweepGradient(
            0.0f to Color.Transparent, 0.5f to Color.Transparent,
            0.7f to radarGreen.copy(alpha = 0.02f),
            0.85f to radarGreen.copy(alpha = 0.15f),
            0.95f to radarGreen.copy(alpha = 0.35f),
            1.0f to radarGreen.copy(alpha = 0.60f)
        )
    }

    val dotPositions = remember(allIps.take(10).hashCode()) {
        allIps.map { alive ->
            val seed = "${alive.ip}:${alive.port}".hashCode()
            val rng = Random(seed)
            val angle = rng.nextFloat() * 360f
            val normalizedPing = (alive.ping.coerceAtMost(1000).toFloat() / 1000f).coerceIn(0.05f, 0.95f)
            val distance = 0.1f + (1f - normalizedPing) * 0.75f
            val pa = rng.nextFloat() * 360f
            DotPos(angle, distance, pa)
        }
    }

    Box(Modifier.fillMaxSize()) {
        ParticleBackground(isLight = isLight, modifier = Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    0 -> ScannerTab(isScanning, scannedCount, aliveCount, deadCount, eta, subnetScanning, allIps, recentProbes, animatedAngle, sweepBrush, dotPositions, isLight, showFullscreenRadar, { showFullscreenRadar = it }, viewModel, context, networkType, selectedOperator, { selectedOperator = it }, desktopMode, networkOverride)
                    1 -> ResultsTab(viewModel, isScanning, aliveCount, allIps, context, lang, isLight, showConfigBuilder, { showConfigBuilder = it }, desktopMode)
                }
            }
        }
    }

    if (showFullscreenRadar) {
        FullscreenRadarDialog(allIps, dotPositions, animatedAngle, sweepBrush, isLight, isScanning) { showFullscreenRadar = false }
    }

    if (showConfigBuilder) {
        ConfigBuilderDialog(viewModel, context, isLight, allIps) { showConfigBuilder = false }
    }
}

private data class DotPos(val angle: Float, val distance: Float, val phase: Float)

@Composable
private fun ScannerTab(
    isScanning: Boolean, scannedCount: Int, aliveCount: Int, deadCount: Int,
    eta: String, subnetScanning: String, allIps: List<AliveIp>, recentProbes: List<String>,
    animatedAngle: Float, sweepBrush: Brush, dotPositions: List<DotPos>,
    isLight: Boolean, showFullscreenRadar: Boolean, onShowFullscreen: (Boolean) -> Unit,
    viewModel: NovaRadarViewModel, context: android.content.Context,
    networkType: String, selectedOperator: String, onOperatorChange: (String) -> Unit,
    desktopMode: Boolean, networkOverride: String
) {
    val lastScanTime by viewModel.lastScanTimestamp.collectAsState()

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(if (isLight) Color(0xFFF0F5FF).copy(alpha = 0.8f) else Color(0xFF0D1628).copy(alpha = 0.9f))
                .border(0.5.dp, Wc.primary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(
                    when {
                        isScanning -> Color(0xFF10B981)
                        allIps.isNotEmpty() -> Color(0xFF3B82F6)
                        else -> Color.Gray.copy(alpha = 0.4f)
                    }
                ))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isScanning) "SCAN ACTIVE" else if (allIps.isNotEmpty()) "${allIps.size} IPS READY" else "SCANNER STANDBY",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                        color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark
                    )
                    Text(subnetScanning.ifEmpty { if (isScanning) "Initializing..." else "Idle" },
                        fontSize = 7.sp, fontFamily = FontFamily.Monospace,
                        color = if (isLight) Color(0xFF4A5568) else Color(0xFF8B95A8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (isScanning) {
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Wc.warning.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("ETA $eta", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Wc.warning)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                val netColor = when (networkType) {
                    "WiFi" -> Color(0xFF3B82F6); "5G" -> Color(0xFF10B981); "4G" -> Color(0xFF8B5CF6)
                    else -> Color.Gray
                }
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(netColor.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(networkType, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = netColor)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Auto" to "", "4G" to "4G", "5G" to "5G", "WiFi" to "WiFi").forEach { (label, key) ->
                val active = networkOverride == key
                Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (active) Wc.primary.copy(alpha = 0.12f) else Color.Transparent)
                    .border(0.5.dp, if (active) Wc.primary.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable(remember { MutableInteractionSource() }, null) { viewModel.setNetworkTypeOverride(key) }
                    .padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                        color = if (active) Wc.primary else Color.Gray.copy(alpha = 0.5f))
                }
            }
        }

        Row(Modifier.fillMaxWidth().height(if (desktopMode) 180.dp else 140.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.15f), modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isLight) Color(0xFF001a0a) else Color(0xFF000d05))
                        .clickable(remember { MutableInteractionSource() }, null) { onShowFullscreen(true) },
                    contentAlignment = Alignment.Center
                ) {
                    RadarCanvas(allIps.take(10), dotPositions, animatedAngle, sweepBrush, isScanning, showLabels = true)
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatWidget(Modifier.weight(1f), "SCANNED", "$scannedCount", Wc.primary, Wc.primary, isLight)
                    StatWidget(Modifier.weight(1f), "ALIVE", "$aliveCount", Wc.success, Wc.success, isLight)
                }
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatWidget(Modifier.weight(1f), "DEAD", "$deadCount", Wc.error, Wc.error, isLight)
                    StatWidget(Modifier.weight(1f), "ETA", eta, Wc.warning, Wc.warning, isLight)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("all" to "\uD83C\uDF10 All", "mci" to "\uD83D\uDCF1 MCI", "mtn" to "\uD83D\uDCF6 MTN", "ict" to "\uD83D\uDCBB ICT").forEach { (key, label) ->
                val active = selectedOperator == key
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (active) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent)
                        .border(0.5.dp, if (active) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .clickable(remember { MutableInteractionSource() }, null) { onOperatorChange(key) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.6f))
                }
            }
        }

        if (lastScanTime > 0) {
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(if (isLight) Color(0xFFEDF2F7) else Color(0xFF1C2333)).padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(lastScanTime))
                Text("Last scan: $timeStr", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color.Gray.copy(alpha = 0.6f))
            }
        }

        val mergedLog = remember(recentProbes, viewModel.logs) {
            val logs = viewModel.logs.value
            val combined = mutableListOf<String>()
            combined.addAll(recentProbes.take(8))
            if (logs.isNotEmpty()) {
                combined.add("\u2500".repeat(20))
                combined.addAll(logs.take(6))
            }
            combined
        }
        WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PROBE FEED", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.5f), letterSpacing = 1.sp)
                TextButton(onClick = { viewModel.clearLogs() }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(20.dp)) {
                    Text("Clear", fontSize = 7.sp, color = Wc.primary.copy(alpha = 0.5f))
                }
            }
            Spacer(Modifier.height(4.dp))
            if (mergedLog.isEmpty()) {
                Text("awaiting scan...", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.2f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    mergedLog.take(10).forEach { entry ->
                        val txtColor = when {
                            entry.contains("\u2713") || entry.contains("ALIVE") || entry.contains("\u2714") -> Wc.success
                            entry.contains("\u2717") || entry.contains("DEAD") || entry.contains("\u2716") -> Wc.error.copy(alpha = 0.5f)
                            entry.startsWith("\u2500") -> Wc.primary.copy(alpha = 0.3f)
                            entry.contains("ms") && !entry.contains("\u2717") -> Wc.primary.copy(alpha = 0.7f)
                            else -> (if (isLight) Color(0xFF4A5568) else Color(0xFF8B95A8)).copy(alpha = 0.6f)
                        }
                        Text(entry, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = txtColor, maxLines = 1)
                    }
                }
            }
        }
    }
}

private val radarGreen = Color(0xFF00FF66)
private val radarGreenBright = Color(0xFF33FF88)

@Composable
private fun RadarCanvas(
    allIps: List<AliveIp>, dotPositions: List<DotPos>,
    animatedAngle: Float, sweepBrush: Brush, isScanning: Boolean,
    showLabels: Boolean = false
) {
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        val outer = r * 0.85f

        val alphaScale = if (isScanning) 1f else 0.55f

        for (i in 1..4) {
            val radius = outer * (i / 4f)
            val ringAlpha = 0.25f + (i / 4f) * 0.15f
            drawCircle(color = radarGreen.copy(alpha = ringAlpha * alphaScale), radius = radius, style = Stroke(1.5.dp.toPx()))
        }

        val chAlpha = 0.18f * alphaScale
        drawLine(color = radarGreen.copy(alpha = chAlpha), start = Offset(c.x - outer, c.y), end = Offset(c.x + outer, c.y), strokeWidth = 1.dp.toPx())
        drawLine(color = radarGreen.copy(alpha = chAlpha), start = Offset(c.x, c.y - outer), end = Offset(c.x, c.y + outer), strokeWidth = 1.dp.toPx())
        val diag = outer * 0.707f
        drawLine(radarGreen.copy(alpha = 0.12f * alphaScale), Offset(c.x - diag, c.y - diag), Offset(c.x + diag, c.y + diag), 0.8.dp.toPx())
        drawLine(radarGreen.copy(alpha = 0.12f * alphaScale), Offset(c.x + diag, c.y - diag), Offset(c.x - diag, c.y + diag), 0.8.dp.toPx())

        val sweepRad = Math.toRadians(animatedAngle.toDouble())
        val sweepEndX = c.x + outer * cos(sweepRad).toFloat()
        val sweepEndY = c.y + outer * sin(sweepRad).toFloat()

        for (i in 1..6) {
            val trailAngle = animatedAngle - i * 2f
            val tRad = Math.toRadians(trailAngle.toDouble())
            val tx = c.x + outer * cos(tRad).toFloat()
            val ty = c.y + outer * sin(tRad).toFloat()
            val trailAlpha = (0.35f * (1f - i / 6f)).coerceIn(0f, 1f)
            drawLine(color = radarGreen.copy(alpha = trailAlpha * alphaScale), start = c, end = Offset(tx, ty), strokeWidth = (2f - i * 0.22f).dp.toPx())
        }

        drawLine(color = radarGreenBright.copy(alpha = 0.95f * alphaScale), start = c, end = Offset(sweepEndX, sweepEndY), strokeWidth = 2.5.dp.toPx())

        drawCircle(color = radarGreen.copy(alpha = 0.9f * alphaScale), radius = 3.dp.toPx())
        drawCircle(color = radarGreen.copy(alpha = 0.25f * alphaScale), radius = 5.dp.toPx(), style = Stroke(0.5.dp.toPx()))

        allIps.take(10).forEachIndexed { index, alive ->
            if (index < dotPositions.size) {
                val dp = dotPositions[index]
                val daRad = Math.toRadians(dp.angle.toDouble())
                val distPx = dp.distance * outer * 0.92f
                val dx = c.x + distPx * cos(daRad).toFloat()
                val dy = c.y + distPx * sin(daRad).toFloat()

                val dotColor = when {
                    alive.ping < 200 -> radarGreen
                    alive.ping < 500 -> Color(0xFFFFCC00)
                    else -> Color(0xFFFF4444)
                }

                val angleDiff = ((animatedAngle - dp.angle + 720f) % 360f) / 360f
                val baseAlpha = if (isScanning) {
                    val highlight = (1f - angleDiff * 4f).coerceIn(0f, 1f)
                    val persist = (1f - ((angleDiff - 0.25f) * 2f).coerceIn(0f, 1f)) * 0.2f
                    highlight + persist
                } else 0.35f
                val alpha = baseAlpha.coerceIn(0.1f, 1f)

                drawCircle(color = dotColor.copy(alpha = 0.3f * alpha), radius = 6.dp.toPx(), center = Offset(dx, dy), style = Stroke(1.dp.toPx()))
                drawCircle(color = dotColor.copy(alpha = alpha), radius = 3.dp.toPx(), center = Offset(dx, dy))

                if (showLabels && index < 5) {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb((alpha.coerceIn(0.4f, 1f) * 255).toInt(), 0, 255, 102)
                        textSize = 14f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawText("${alive.ping}ms", dx, dy - 6.dp.toPx(), paint)
                }
            }
        }
    }
}

@Composable
private fun FullscreenRadarDialog(
    allIps: List<AliveIp>, dotPositions: List<DotPos>,
    animatedAngle: Float, sweepBrush: Brush, isLight: Boolean, isScanning: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0D1219))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RADAR SCOPE", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = radarGreen, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp))
                    .background(if (isLight) Color(0xFF001a0a) else Color(0xFF000d05))
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val outer = r * 0.85f
                        val alphaScale = if (isScanning) 1f else 0.50f

                        for (i in 1..4) {
                            drawCircle(color = radarGreen.copy(alpha = (0.12f + i/4f*0.12f) * alphaScale), radius = outer * (i / 4f), style = Stroke(1.5.dp.toPx()))
                        }
                        drawLine(color = radarGreen.copy(alpha = 0.10f * alphaScale), start = Offset(c.x - outer, c.y), end = Offset(c.x + outer, c.y), strokeWidth = 0.5.dp.toPx())
                        drawLine(color = radarGreen.copy(alpha = 0.10f * alphaScale), start = Offset(c.x, c.y - outer), end = Offset(c.x, c.y + outer), strokeWidth = 0.5.dp.toPx())
                        val diag = outer * 0.707f
                        drawLine(radarGreen.copy(alpha = 0.07f * alphaScale), Offset(c.x - diag, c.y - diag), Offset(c.x + diag, c.y + diag), 0.5.dp.toPx())
                        drawLine(radarGreen.copy(alpha = 0.07f * alphaScale), Offset(c.x + diag, c.y - diag), Offset(c.x - diag, c.y + diag), 0.5.dp.toPx())

                        val sweepRad = Math.toRadians(animatedAngle.toDouble())
                        val sweepEndX = c.x + outer * cos(sweepRad).toFloat()
                        val sweepEndY = c.y + outer * sin(sweepRad).toFloat()

                        for (i in 1..6) {
                            val trailAngle = animatedAngle - i * 1.8f
                            val tRad = Math.toRadians(trailAngle.toDouble())
                            val tx = c.x + outer * cos(tRad).toFloat()
                            val ty = c.y + outer * sin(tRad).toFloat()
                            val trailAlpha = (0.25f * (1f - i / 6f)).coerceIn(0f, 1f)
                            drawLine(color = radarGreen.copy(alpha = trailAlpha * alphaScale), start = c, end = Offset(tx, ty), strokeWidth = (2f - i * 0.22f).dp.toPx())
                        }

                        drawLine(color = radarGreen.copy(alpha = 0.85f * alphaScale), start = c, end = Offset(sweepEndX, sweepEndY), strokeWidth = 2.dp.toPx())

                        drawCircle(color = radarGreen.copy(alpha = 0.9f * alphaScale), radius = 4.dp.toPx())
                        drawCircle(color = radarGreen.copy(alpha = 0.25f * alphaScale), radius = 6.dp.toPx(), style = Stroke(0.5.dp.toPx()))

                        allIps.take(10).forEachIndexed { index, alive ->
                            if (index < dotPositions.size) {
                                val dp = dotPositions[index]
                                val daRad = Math.toRadians(dp.angle.toDouble())
                                val distPx = dp.distance * outer * 0.92f
                                val dx = c.x + distPx * cos(daRad).toFloat()
                                val dy = c.y + distPx * sin(daRad).toFloat()
                                val dotColor = when { alive.ping < 200 -> radarGreen; alive.ping < 500 -> Color(0xFFFFCC00); else -> Color(0xFFFF4444) }
                                val angleDiff = ((animatedAngle - dp.angle + 720f) % 360f) / 360f
                                val baseAlpha = if (isScanning) {
                                    ((1f - angleDiff * 4f).coerceIn(0f, 1f) + (1f - ((angleDiff - 0.25f) * 2f).coerceIn(0f, 1f)) * 0.2f)
                                } else 0.35f
                                val alpha = baseAlpha.coerceIn(0.1f, 1f)
                                drawCircle(color = dotColor.copy(alpha = 0.3f * alpha), radius = 7.dp.toPx(), center = Offset(dx, dy), style = Stroke(1.dp.toPx()))
                                drawCircle(color = dotColor.copy(alpha = alpha), radius = 3.5f.dp.toPx(), center = Offset(dx, dy))
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb((alpha.coerceIn(0.4f, 1f) * 255).toInt(), 0, 255, 102)
                                    textSize = 16f; textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
                                }
                                drawContext.canvas.nativeCanvas.drawText("${alive.ping}ms", dx, dy - 8.dp.toPx(), paint)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontWeight = FontWeight.Bold, color = Wc.primary)
                    }
                }
                allIps.take(10).forEachIndexed { i, alive ->
                    val rank = pingRank(alive.ping)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${i + 1} ${alive.ip}:${alive.port}", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark)
                        Text("${alive.ping}ms", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = rank.color)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigBuilderDialog(
    viewModel: NovaRadarViewModel, context: android.content.Context, isLight: Boolean,
    allIps: List<AliveIp>, onDismiss: () -> Unit
) {
    val cfgOutput by viewModel.cfgOutput.collectAsState()
    val cfgUuid by viewModel.cfgUuid.collectAsState()
    val cfgSni by viewModel.cfgSni.collectAsState()
    val cfgNetwork by viewModel.cfgNetwork.collectAsState()
    val cfgSecurity by viewModel.cfgSecurity.collectAsState()
    val cfgPath by viewModel.cfgPath.collectAsState()
    var cfgTab by remember { mutableStateOf("vless") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0D1219))
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("CONFIG BUILDER", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary, letterSpacing = 1.sp)
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("vless" to "VLESS", "vmess" to "VMess", "clash" to "Clash", "singbox" to "SingBox").forEach { (key, label) ->
                        val active = cfgTab == key
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (active) Wc.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .border(0.5.dp, if (active) Wc.primary.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .clickable(remember { MutableInteractionSource() }, null) { cfgTab = key }
                            .padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                                color = if (active) Wc.primary else Color.Gray.copy(alpha = 0.5f))
                        }
                    }
                }

                WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.08f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("UUID", Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                            OutlinedTextField(value = cfgUuid, onValueChange = { viewModel.setCfgUuid(it) }, modifier = Modifier.weight(1f), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace))
                            Box(Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)).background(Wc.primary.copy(alpha = 0.1f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.generateUuid() }, contentAlignment = Alignment.Center) {
                                Text("\u21BB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Wc.primary)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("SNI", Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                            OutlinedTextField(value = cfgSni, onValueChange = { viewModel.setCfgSni(it) }, modifier = Modifier.weight(1f), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace))
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Net", Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("ws", "tcp", "grpc", "h2").forEach { n ->
                                    val a = cfgNetwork == n
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                                        .background(if (a) Wc.primary.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(0.5.dp, if (a) Wc.primary.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .clickable(remember { MutableInteractionSource() }, null) { viewModel.setCfgNetwork(n) }
                                        .padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                        Text(n, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (a) Wc.primary else Color.Gray.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Sec", Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("tls", "none").forEach { s ->
                                    val a = cfgSecurity == s
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                                        .background(if (a) Wc.primary.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(0.5.dp, if (a) Wc.primary.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .clickable(remember { MutableInteractionSource() }, null) { viewModel.setCfgSecurity(s) }
                                        .padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                        Text(s, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (a) Wc.primary else Color.Gray.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Path", Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                            OutlinedTextField(value = cfgPath, onValueChange = { viewModel.setCfgPath(it) }, modifier = Modifier.weight(1f), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace))
                        }
                    }
                }

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Wc.primary.copy(alpha = 0.12f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.buildConfig(cfgTab) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("BUILD ${cfgTab.uppercase()} CONFIG", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                }

                if (cfgOutput.isNotEmpty()) {
                    WidgetCard(isLightTheme = isLight, borderColor = Wc.success.copy(alpha = 0.12f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("OUTPUT (${cfgOutput.lines().size} lines)", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Wc.success.copy(alpha = 0.6f))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Wc.primary.copy(alpha = 0.1f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.copyCfgOutput(context) }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.ContentCopy, null, tint = Wc.primary, modifier = Modifier.size(10.dp))
                                        Text("Copy", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Wc.primary)
                                    }
                                }
                                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Wc.success.copy(alpha = 0.1f)).clickable(remember { MutableInteractionSource() }, null) { viewModel.saveCfgToFile(context, "nova-$cfgTab-config.txt") }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.Save, null, tint = Wc.success, modifier = Modifier.size(10.dp))
                                        Text("Save", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Wc.success)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(cfgOutput, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsTab(
    viewModel: NovaRadarViewModel, isScanning: Boolean, aliveCount: Int, allIps: List<AliveIp>,
    context: android.content.Context, lang: AppLanguage, isLight: Boolean,
    showConfigBuilder: Boolean, onShowConfigBuilder: (Boolean) -> Unit,
    desktopMode: Boolean
) {
    val speedResults by viewModel.speedResults.collectAsState()
    val runningSpeedTests by viewModel.runningSpeedTests.collectAsState()
    val lastScanTime by viewModel.lastScanTimestamp.collectAsState()
    var maxPing by remember { mutableStateOf(9999) }
    var showPingSlider by remember { mutableStateOf(false) }

    val filteredIps = if (maxPing >= 9999) allIps else allIps.filter { it.ping <= maxPing }

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (isScanning) Wc.warning else if (allIps.isNotEmpty()) Wc.success else Color.Gray.copy(alpha = 0.3f)))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isScanning) "SCANNING..." else if (allIps.isNotEmpty()) "${if (maxPing >= 9999) allIps.size else "${filteredIps.size}/${allIps.size}"} TARGETS" else "NO TARGETS",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                            color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark
                        )
                        if (allIps.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            Text("Avg ${allIps.map { it.ping }.average().toLong()}ms", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { showPingSlider = !showPingSlider }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.FilterList, null, tint = if (maxPing >= 9999) Wc.primary else Wc.warning, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { viewModel.exportResultsToTxtFile(context) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Save, null, tint = Wc.primary, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { onShowConfigBuilder(true) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Build, null, tint = Wc.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (showPingSlider) {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isLight) Color(0xFFEDF2F7) else Color(0xFF1C2333)).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("PING", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                        Slider(
                            value = maxPing.toFloat(), onValueChange = { maxPing = it.toInt() },
                            valueRange = 50f..2000f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = Wc.primary, activeTrackColor = Wc.primary.copy(alpha = 0.5f))
                        )
                        Text("${maxPing}ms", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (maxPing < 9999) Wc.warning else if (isLight) Color(0xFF4A5568) else Color.Gray)
                        if (maxPing < 9999) {
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Wc.error.copy(alpha = 0.15f)).clickable(remember { MutableInteractionSource() }, null) { maxPing = 9999 }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, null, tint = Wc.error, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }

        if (filteredIps.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Clash" to { viewModel.exportClash(context) }, "V2Ray" to { viewModel.exportV2Ray(context) }, "VLESS" to { viewModel.exportVLESS(context) }, "SingBox" to { viewModel.exportSingBox(context) }).forEach { (label, action) ->
                            Box(Modifier.weight(1f).height(30.dp).clip(RoundedCornerShape(8.dp)).background(Wc.primary.copy(alpha = 0.08f)).border(0.5.dp, Wc.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).clickable(remember { MutableInteractionSource() }, null, onClick = action), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Wc.primary)
                                    Icon(Icons.Default.ContentCopy, null, tint = Wc.primary.copy(alpha = 0.5f), modifier = Modifier.size(8.dp))
                                }
                            }
                        }
                    }

                    val rankCounts = filteredIps.groupBy { pingRank(it.ping) }.mapValues { it.value.size }
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isLight) Color(0xFFEDF2F7) else Color(0xFF1C2333)).padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${filteredIps.size} IPs", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark)
                            if (lastScanTime > 0) {
                                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date(lastScanTime))
                                Text("~$timeStr", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Color.Gray.copy(alpha = 0.5f))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Rank.entries.forEach { r ->
                                val cnt = rankCounts[r] ?: 0
                                if (cnt > 0) {
                                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(r.color.copy(alpha = 0.15f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                        Text("${r.label}$cnt", fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = r.color)
                                    }
                                }
                            }
                        }
                        Text("Avg ${filteredIps.map { it.ping }.average().toLong()}ms", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    }

                    val chartIps = filteredIps.sortedBy { it.ping }.take(15)
                    val maxChartPing = chartIps.maxOfOrNull { it.ping }?.coerceAtLeast(1) ?: 1
                    WidgetCard(isLightTheme = isLight, borderColor = Wc.primary.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                        Text("PING COMPARISON", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Wc.primary.copy(alpha = 0.5f), letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        chartIps.forEachIndexed { idx, ip ->
                            val rank = pingRank(ip.ping)
                            val bw = (ip.ping.toFloat() / maxChartPing).coerceIn(0.02f, 1f)
                            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("#${idx + 1}", Modifier.width(18.dp), fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF4A5568) else Color.Gray.copy(alpha = 0.5f))
                                Text(ip.ip.takeLast(8), Modifier.width(52.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF1A202C) else Wc.onSurfaceDark, maxLines = 1)
                                Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(2.dp)).background(rank.color.copy(alpha = 0.1f))) {
                                    Box(Modifier.fillMaxWidth(bw).fillMaxHeight().background(rank.color.copy(alpha = 0.5f)))
                                }
                                Text("${ip.ping}ms", Modifier.width(30.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = rank.color)
                            }
                        }
                    }

                    var showCopyAllMenu by remember { mutableStateOf(false) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Wc.primary.copy(alpha = 0.08f)).border(0.5.dp, Wc.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).clickable(remember { MutableInteractionSource() }, null) { viewModel.copyAllIpsOnly(context) }.padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text("Copy IPs (No Suffix)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Wc.primary)
                        }
                        Box {
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Wc.primary.copy(alpha = 0.08f)).border(0.5.dp, Wc.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).clickable(remember { MutableInteractionSource() }, null) { showCopyAllMenu = true }.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ContentCopy, null, tint = Wc.primary, modifier = Modifier.size(12.dp))
                                    Text("COPY ALL", fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Wc.primary)
                                }
                            }
                            DropdownMenu(expanded = showCopyAllMenu, onDismissRequest = { showCopyAllMenu = false }) {
                                DropdownMenuItem(text = { Text("Copy IPs (No Suffix)", fontSize = 11.sp) }, onClick = { showCopyAllMenu = false; viewModel.copyAllIpsOnly(context) })
                                DropdownMenuItem(text = { Text("Copy IP:Port (No Suffix)", fontSize = 11.sp) }, onClick = { showCopyAllMenu = false; viewModel.copyAllIpsPort(context) })
                                DropdownMenuItem(text = { Text("Copy with Nova Suffix", fontSize = 11.sp) }, onClick = { showCopyAllMenu = false; viewModel.copyAllToClipboard(context) })
                            }
                        }
                    }
                }
            }
        }

        if (filteredIps.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isScanning) "AWAITING TARGETS" else "NO TARGETS", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = (if (isScanning) Wc.warning else Wc.error).copy(alpha = 0.5f))
                        Spacer(Modifier.height(4.dp))
                        Text(if (isScanning) "IPs appear as verified" else "Start a scan", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray.copy(alpha = 0.4f))
                    }
                }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (isLight) Color(0xFFEDF2F7) else Color(0xFF1C2333)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("#", Modifier.width(22.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    Text("IP", Modifier.weight(1f), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    Text("PING", Modifier.width(34.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    Text("HTTP", Modifier.width(30.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    Text("SPD", Modifier.width(28.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                    Text("ACT", Modifier.width(48.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF4A5568) else Color.Gray)
                }
            }

            itemsIndexed(filteredIps, key = { _, ip -> "${ip.ip}:${ip.port}" }) { index, alive ->
                val speedKey = "${alive.ip}:${alive.port}"
                val speedStr = speedResults[speedKey] ?: "--"
                val isTesting = speedKey in runningSpeedTests
                val rank = pingRank(alive.ping)
                val pingColor = rank.color
                val httpColor = when { alive.httpPing < 0 -> Color.Gray.copy(alpha = 0.3f); alive.httpPing < 300 -> Wc.success; alive.httpPing < 600 -> Wc.warning; else -> Wc.error }
                var showCopyMenu by remember { mutableStateOf(false) }

                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (index % 2 == 0) Color.Transparent else (if (isLight) Color(0xFFF7FAFC) else Color(0xFF111827).copy(alpha = 0.3f))).padding(horizontal = 6.dp, vertical = 4.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}", Modifier.width(22.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = if (isLight) Color(0xFF718096) else Color.Gray.copy(alpha = 0.6f))
                            Column(Modifier.weight(1f)) {
                                Text(alive.ip, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (isLight) Color(0xFF1A202C) else Color(0xFFE8ECF4), maxLines = 1)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(":${alive.port}#Nova-${alive.novaId}", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Wc.success.copy(alpha = 0.7f))
                                    Box(Modifier.clip(RoundedCornerShape(2.dp)).background(rank.color.copy(alpha = 0.15f)).padding(horizontal = 3.dp, vertical = 1.dp)) {
                                        Text(rank.label, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = rank.color)
                                    }
                                    if (alive.port in tlsPorts) {
                                        Box(Modifier.clip(RoundedCornerShape(2.dp)).background(Wc.primary.copy(alpha = 0.1f)).padding(horizontal = 3.dp, vertical = 1.dp)) {
                                            Text("TLS", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Wc.primary.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                            }
                            Text("${alive.ping}", Modifier.width(34.dp), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = pingColor)
                            Text(if (alive.httpPing > 0) "${alive.httpPing}" else "--", Modifier.width(30.dp), fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = httpColor)
                            Text(if (isTesting) "..." else speedStr, Modifier.width(28.dp), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Wc.warning.copy(alpha = 0.8f))

                            Row(Modifier.width(48.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(22.dp).clip(RoundedCornerShape(4.dp)).background(if (isTesting) Wc.info.copy(alpha = 0.05f) else Wc.info.copy(alpha = 0.12f)).then(if (!isTesting) Modifier.clickable(remember { MutableInteractionSource() }, null) { viewModel.runSpeedTest(alive.ip, alive.port) } else Modifier), contentAlignment = Alignment.Center) {
                                    Text("\u26A1", fontSize = 9.sp)
                                }
                                Box {
                                    Box(Modifier.size(22.dp).clip(RoundedCornerShape(4.dp)).background(Wc.success.copy(alpha = 0.12f)).clickable(remember { MutableInteractionSource() }, null) { showCopyMenu = true }, contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.ContentCopy, null, tint = Wc.success, modifier = Modifier.size(10.dp))
                                    }
                                    DropdownMenu(expanded = showCopyMenu, onDismissRequest = { showCopyMenu = false }) {
                                        DropdownMenuItem(text = { Text("Copy IP (No Suffix)", fontSize = 11.sp) }, onClick = { showCopyMenu = false; viewModel.copyIndividualIpToClipboard(context, alive) })
                                        DropdownMenuItem(text = { Text("Copy IP:Port (No Suffix)", fontSize = 11.sp) }, onClick = { showCopyMenu = false; viewModel.copyIpPortOnly(context, alive) })
                                        DropdownMenuItem(text = { Text("Copy with Nova Suffix", fontSize = 12.sp) }, onClick = { showCopyMenu = false; viewModel.copyIndividualToClipboard(context, alive) })
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().padding(start = 22.dp).height(1.dp)) {
                            val bw = (1f - (alive.ping.coerceAtMost(1000).toFloat() / 1000f)).coerceIn(0.02f, 0.98f)
                            Box(Modifier.fillMaxWidth(bw).fillMaxHeight().background(pingColor.copy(alpha = 0.3f)))
                        }
                    }
                }
            }
        }
    }
}
