package com.novascanner.network.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = Primary, secondary = Secondary, background = Background,
    surface = Surface, surfaceVariant = SurfaceVariant, error = Error,
    onPrimary = TextPrimary, onSecondary = TextPrimary, onBackground = TextPrimary,
    onSurface = TextPrimary, onSurfaceVariant = TextSecondary, outline = BorderColor
)

@Composable
fun NovaRadarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, typography = AppTypography, content = content)
}
