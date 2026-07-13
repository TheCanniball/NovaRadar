package com.novascanner.network.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ProbeCard(result: ProbeResult, suffix: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gradeColor = when (result.grade) { Grade.SS -> GradeSS; Grade.S -> GradeS; Grade.A -> GradeA; Grade.B -> GradeB; Grade.C -> GradeC; Grade.D -> GradeD; Grade.F -> GradeF }
    val bg = if (result.isWorking) Surface else SurfaceVariant.copy(alpha = 0.5f)
    Card(modifier = modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(gradeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                Text(result.grade.display, color = gradeColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(result.ip, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                val sf = if (suffix.isBlank()) "" else suffix
                Text("${result.ip}:${result.port}$sf", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${result.tcpLatencyMs}ms", color = gradeColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                if (result.colo.isNotBlank()) Text(result.colo, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) { Text(text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) }
