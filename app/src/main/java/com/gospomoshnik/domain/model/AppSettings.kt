package com.gospomoshnik.domain.model

import androidx.compose.runtime.Immutable

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Масштаб шрифта — для читабельности (молодым и пожилым). */
enum class FontSize(val scale: Float, val label: String) {
    SMALL(0.9f, "Мелкий"),
    NORMAL(1.0f, "Обычный"),
    LARGE(1.15f, "Крупный"),
    HUGE(1.3f, "Очень крупный")
}

@Immutable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSize: FontSize = FontSize.NORMAL
)
