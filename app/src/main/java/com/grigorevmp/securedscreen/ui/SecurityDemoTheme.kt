package com.grigorevmp.securedscreen.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A3526),
    onPrimary = Color(0xFFFFF8F2),
    primaryContainer = Color(0xFFFFD7CC),
    onPrimaryContainer = Color(0xFF2E130D),
    secondary = Color(0xFF3A6B6F),
    onSecondary = Color(0xFFF3FEFE),
    secondaryContainer = Color(0xFFD2ECEE),
    onSecondaryContainer = Color(0xFF102C30),
    tertiary = Color(0xFF596430),
    tertiaryContainer = Color(0xFFDDE8AF),
    onTertiaryContainer = Color(0xFF1A1F08),
    background = Color(0xFFF6F1E8),
    onBackground = Color(0xFF1F1B16),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFE8DED0),
    onSurfaceVariant = Color(0xFF4E453B),
    outline = Color(0xFF807568),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB59F),
    onPrimary = Color(0xFF4A2117),
    primaryContainer = Color(0xFF643021),
    onPrimaryContainer = Color(0xFFFFD7CC),
    secondary = Color(0xFF9ED0D4),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF1F4F53),
    onSecondaryContainer = Color(0xFFD2ECEE),
    tertiary = Color(0xFFC1CC95),
    onTertiary = Color(0xFF2B340F),
    tertiaryContainer = Color(0xFF414A23),
    onTertiaryContainer = Color(0xFFDDE8AF),
    background = Color(0xFF151310),
    onBackground = Color(0xFFEAE1D7),
    surface = Color(0xFF1D1B18),
    onSurface = Color(0xFFEAE1D7),
    surfaceVariant = Color(0xFF4E453B),
    onSurfaceVariant = Color(0xFFD3C4B6),
    outline = Color(0xFF9C8F82),
)

@Composable
fun SecurityDemoTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            DarkColors
        } else {
            LightColors
        },
        content = content,
    )
}
