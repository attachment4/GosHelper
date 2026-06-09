package com.gospomoshnik.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Основной бренд-цвет #4338CA (Indigo-700)
private val brandColor      = Color(0xFF4338CA)
private val brandContainer  = Color(0xFFEEF2FF)
private val onBrandContainer = Color(0xFF3730A3)

private val LightColorScheme = lightColorScheme(
    primary              = brandColor,
    onPrimary            = Color.White,
    primaryContainer     = brandContainer,
    onPrimaryContainer   = onBrandContainer,
    secondary            = Color(0xFF059669),
    background           = Color(0xFFF9FAFB),
    surface              = Color.White,
    onBackground         = Color(0xFF111827),
    onSurface            = Color(0xFF111827),
    onSurfaceVariant     = Color(0xFF6B7280),
    outline              = Color(0xFFE5E7EB)
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF818CF8),
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = Color(0xFF3730A3),
    onPrimaryContainer   = Color(0xFFC7D2FE),
    background           = Color(0xFF0F0F23),
    surface              = Color(0xFF1A1A2E),
    onBackground         = Color(0xFFF9FAFB),
    onSurface            = Color(0xFFF3F4F6),
    onSurfaceVariant     = Color(0xFF9CA3AF)
)

@Composable
fun GospomoshnikTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
