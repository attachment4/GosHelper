package com.gospomoshnik.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.data.settings.SettingsRepository
import com.gospomoshnik.domain.model.AppSettings
import com.gospomoshnik.domain.model.FontSize
import com.gospomoshnik.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repository.setTheme(mode) }
    fun setFontSize(size: FontSize) = viewModelScope.launch { repository.setFontSize(size) }
}
