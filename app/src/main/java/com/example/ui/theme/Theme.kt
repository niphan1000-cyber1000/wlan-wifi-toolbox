package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = CyberEmerald,
    tertiary = CyberPurple,
    background = SlateDarkBg,
    surface = SlateSurface,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = RedSignal
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
