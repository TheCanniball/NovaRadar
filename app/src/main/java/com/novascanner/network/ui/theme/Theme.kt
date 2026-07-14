package com.novascanner.network.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = Primary, secondary = Secondary, background = DarkBackground,
    surface = DarkSurface, surfaceVariant = DarkSurfaceVariant, error = Error,
    onPrimary = DarkTextPrimary, onSecondary = DarkTextPrimary, onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary, onSurfaceVariant = DarkTextSecondary, outline = DarkBorder
)

private val LightScheme = lightColorScheme(
    primary = Primary, secondary = Secondary, background = LightBackground,
    surface = LightSurface, surfaceVariant = LightSurfaceVariant, error = Error,
    onPrimary = LightTextPrimary, onSecondary = LightTextPrimary, onBackground = LightTextPrimary,
    onSurface = LightTextPrimary, onSurfaceVariant = LightTextSecondary, outline = LightBorder
)

@Composable
fun NovaRadarTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isDark) DarkScheme else LightScheme, typography = AppTypography, content = content)
}
