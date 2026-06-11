package com.gospomoshnik.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gospomoshnik.domain.model.AppSettings
import com.gospomoshnik.domain.model.FontSize
import com.gospomoshnik.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val FONT  = stringPreferencesKey("font_size")
    }

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            fontSize  = prefs[Keys.FONT]?.let { runCatching { FontSize.valueOf(it) }.getOrNull() }
                ?: FontSize.NORMAL
        )
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.settingsStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setFontSize(size: FontSize) {
        context.settingsStore.edit { it[Keys.FONT] = size.name }
    }
}
