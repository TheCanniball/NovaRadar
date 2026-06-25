package com.novaradar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.novaradar.app.ui.viewmodel.AppTheme

val PrismDarkBg = Color(0xFF05060A)
val PrismDarkSurface = Color(0xFF090B12)
val PrismDarkPrimary = Color(0xFF22D3EE)
val PrismDarkSecondary = Color(0xFFA855F7)
val PrismDarkTertiary = Color(0xFF34D399)
val PrismDarkOnBg = Color(0xFFEEF1F7)

val PrismLightBg = Color(0xFFF7F8FC)
val PrismLightSurface = Color(0xFFFFFFFF)
val PrismLightPrimary = Color(0xFF0891B2)
val PrismLightSecondary = Color(0xFF9333EA)
val PrismLightTertiary = Color(0xFF0D9488)
val PrismLightOnBg = Color(0xFF0D1117)

private val PrismDarkColorScheme = darkColorScheme(
    primary = PrismDarkPrimary,
    onPrimary = Color(0xFF05060A),
    secondary = PrismDarkSecondary,
    onSecondary = Color.White,
    tertiary = PrismDarkTertiary,
    background = PrismDarkBg,
    onBackground = PrismDarkOnBg,
    surface = PrismDarkSurface,
    onSurface = PrismDarkOnBg
)

private val PrismLightColorScheme = lightColorScheme(
    primary = PrismLightPrimary,
    onPrimary = Color.White,
    secondary = PrismLightSecondary,
    onSecondary = Color.White,
    tertiary = PrismLightTertiary,
    background = PrismLightBg,
    onBackground = PrismLightOnBg,
    surface = PrismLightSurface,
    onSurface = PrismLightOnBg
)

@Composable
fun NovaRadarTheme(
    theme: AppTheme = AppTheme.PRISM_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.PRISM_DARK -> PrismDarkColorScheme
        AppTheme.PRISM_LIGHT -> PrismLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
