package com.epn.transitagent.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentCyan,
    background = AppBackground,
    surface = CardSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = OutlineSoft,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = Typography,
        content = content
    )
}
