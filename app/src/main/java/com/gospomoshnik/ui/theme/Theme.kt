package com.gospomoshnik.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Палитра в стилистике государственных сервисов РФ (Госуслуги):
 * фирменный синий, светлый нейтральный фон, тёмно-синий текст,
 * белые карточки, минимум декора.
 */
object GosColors {
    val Blue        = Color(0xFF0D4CD3)   // фирменный синий Госуслуг
    val BlueDark    = Color(0xFF073EAE)   // нажатое состояние / градиенты
    val BlueLight   = Color(0xFFE4ECFE)   // светло-синие подложки
    val BlueMid     = Color(0xFFB6CCF7)   // обводки активных полей
    val Background  = Color(0xFFF4F6FB)   // фон экранов
    val Card        = Color(0xFFFFFFFF)   // карточки
    val TextPrimary = Color(0xFF0B1F33)   // основной текст (тёмно-синий)
    val TextSecond  = Color(0xFF66727F)   // вторичный текст
    val Divider     = Color(0xFFE3E6EB)
    val Green       = Color(0xFF0FA958)
    val GreenLight  = Color(0xFFE2F5EA)
    val Red         = Color(0xFFE7372F)
    val RedLight    = Color(0xFFFDEBEA)
    val Amber       = Color(0xFFB45309)
    val AmberLight  = Color(0xFFFFF1D6)
}

private val LightColorScheme = lightColorScheme(
    primary              = GosColors.Blue,
    onPrimary            = Color.White,
    primaryContainer     = GosColors.BlueLight,
    onPrimaryContainer   = GosColors.BlueDark,
    secondary            = GosColors.Green,
    background           = GosColors.Background,
    surface              = GosColors.Card,
    surfaceVariant       = GosColors.Background,
    onBackground         = GosColors.TextPrimary,
    onSurface            = GosColors.TextPrimary,
    onSurfaceVariant     = GosColors.TextSecond,
    outline              = GosColors.Divider,
    error                = GosColors.Red,
    errorContainer       = GosColors.RedLight,
    onErrorContainer     = GosColors.Red
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF6E9BFF),
    onPrimary            = Color(0xFF06255C),
    primaryContainer     = Color(0xFF143A85),
    onPrimaryContainer   = GosColors.BlueMid,
    background           = Color(0xFF0E1621),
    surface              = Color(0xFF182433),
    onBackground         = Color(0xFFF4F6FB),
    onSurface            = Color(0xFFE9EDF2),
    onSurfaceVariant     = Color(0xFF8E99A6)
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
