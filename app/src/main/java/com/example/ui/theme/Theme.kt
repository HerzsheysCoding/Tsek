package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PaperColorScheme = lightColorScheme(
    primary = BallpenBlue,
    onPrimary = Paper,
    primaryContainer = Paper,
    onPrimaryContainer = Ink,
    secondary = BallpenBlue,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Ink,
    outline = PadBlue,
    error = CheckerRed,
    onError = Paper
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PaperColorScheme,
        typography = Typography,
        content = content
    )
}
