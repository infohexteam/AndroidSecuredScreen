package com.grigorevmp.attacker.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF29443B),
    onPrimary = Color(0xFFF7FFF9),
    primaryContainer = Color(0xFFCBE8DA),
    onPrimaryContainer = Color(0xFF0D1F19),
    secondary = Color(0xFF7A3526),
    onSecondary = Color(0xFFFFF8F2),
    secondaryContainer = Color(0xFFFFD7CC),
    onSecondaryContainer = Color(0xFF2E130D),
    background = Color(0xFFF4F0EA),
    onBackground = Color(0xFF181612),
    surface = Color(0xFFFFFBF7),
    onSurface = Color(0xFF181612),
    surfaceVariant = Color(0xFFE0DBD2),
    onSurfaceVariant = Color(0xFF48443D),
    outline = Color(0xFF79746D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8D5C3),
    onPrimary = Color(0xFF113329),
    primaryContainer = Color(0xFF24493D),
    onPrimaryContainer = Color(0xFFCBE8DA),
    secondary = Color(0xFFFFB59F),
    onSecondary = Color(0xFF4A2117),
    secondaryContainer = Color(0xFF643021),
    onSecondaryContainer = Color(0xFFFFD7CC),
    background = Color(0xFF121315),
    onBackground = Color(0xFFE7E2DA),
    surface = Color(0xFF1B1C1E),
    onSurface = Color(0xFFE7E2DA),
    surfaceVariant = Color(0xFF44474C),
    onSurfaceVariant = Color(0xFFC4C7CD),
    outline = Color(0xFF8E9196),
)

@Composable
fun AttackerTheme(
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
