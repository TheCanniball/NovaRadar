package com.novascanner.network.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novascanner.network.scanner.Grade
import com.novascanner.network.scanner.ProbeResult
import com.novascanner.network.ui.theme.*

@Composable
fun NovaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color = Primary) {
    Button(onClick = onClick, modifier = modifier.height(48.dp), enabled = enabled,
        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun NovaField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, singleLine: Boolean = true, lines: Int = 1) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier, singleLine = singleLine, minLines = lines, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
            focusedLabelColor = Primary, cursorColor = Primary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
}

@Composable
fun ProbeCard(result: ProbeResult, suffix: String, onCopy: () -> Unit, isFavorite: Boolean, onToggleFavorite: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val gradeColor = when (result.grade) { Grade.SS -> GradeSS; Grade.S -> GradeS; Grade.A -> GradeA; Grade.B -> GradeB; Grade.C -> GradeC; Grade.D -> GradeD; Grade.F -> GradeF }
    val bg = if (result.isWorking) Surface else SurfaceVariant.copy(alpha = 0.5f)
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, bottom = 6.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(gradeColor.copy(alpha = 0.2f))
                    .clickable { expanded = !expanded }, contentAlignment = Alignment.Center) {
                    Text(result.grade.display, color = gradeColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).clickable { expanded = !expanded }) {
                    Text(result.ip, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    val sf = if (suffix.isBlank()) "" else suffix
                    Text("${result.ip}:${result.port}$sf", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite", tint = if (isFavorite) GradeB else TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) {
                    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(Modifier.height(6.dp))
                    DetailRow("TCP", "${result.tcpLatencyMs}ms", gradeColor)
                    DetailRow("TLS", "${result.tlsLatencyMs}ms", TextSecondary)
                    DetailRow("HTTP", "${result.httpLatencyMs}ms", TextSecondary)
                    if (result.colo.isNotBlank()) DetailRow("Colo", result.colo, TextSecondary)
                    DetailRow("Port", "${result.port}", TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionTitle(text: String) { Text(text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) }

@Composable
fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(color = if (selected) Primary.copy(alpha = 0.2f) else SurfaceVariant,
        shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { onClick() }) {
        Text(label, color = if (selected) Primary else TextSecondary, fontSize = 12.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

data class ScanPreset(val name: String, val cidr: String, val sni: String, val port: String)
object Presets {
    val cloudflare = ScanPreset("Cloudflare", "104.16.0.0/12", "speed.cloudflare.com", "443")
    val cloudflare2 = ScanPreset("CF 2", "1.1.1.0/24", "cloudflare.com", "443")
    val fastly = ScanPreset("Fastly", "151.101.0.0/16", "www.fastly.com", "443")
    val akamai = ScanPreset("Akamai", "23.0.0.0/12", "www.akamai.com", "443")
    val all = listOf(cloudflare, cloudflare2, fastly, akamai)
}
