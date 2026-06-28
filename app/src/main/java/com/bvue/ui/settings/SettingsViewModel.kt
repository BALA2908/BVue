package com.bvue.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.settings.SettingsRepository
import com.bvue.domain.model.AppSettings
import com.bvue.domain.model.QualityPref
import com.bvue.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setQuality(quality: QualityPref) = viewModelScope.launch { repo.setDefaultQuality(quality) }
    fun setAudioLanguage(code: String) = viewModelScope.launch { repo.setPreferredAudioLanguage(code) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setAudioOnly(enabled: Boolean) = viewModelScope.launch { repo.setAudioOnly(enabled) }
    fun setSponsorBlockEnabled(enabled: Boolean) = viewModelScope.launch { repo.setSponsorBlockEnabled(enabled) }
    fun setSponsorCategories(categories: Set<String>) = viewModelScope.launch { repo.setSponsorCategories(categories) }
}
