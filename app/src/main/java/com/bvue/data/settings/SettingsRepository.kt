package com.bvue.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bvue.domain.model.AppSettings
import com.bvue.domain.model.QualityPref
import com.bvue.domain.model.SponsorCategory
import com.bvue.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bvue_settings")

/** Reactive app settings backed by DataStore. Defaults match this user's preferences (Tamil audio, 4K). */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val QUALITY = stringPreferencesKey("default_quality")
        val AUDIO_LANG = stringPreferencesKey("preferred_audio_language")
        val THEME = stringPreferencesKey("theme_mode")
        val AUDIO_ONLY = booleanPreferencesKey("audio_only")
        val SPEED = floatPreferencesKey("playback_speed")
        val SB_ENABLED = booleanPreferencesKey("sponsorblock_enabled")
        val SB_CATS = stringPreferencesKey("sponsorblock_categories")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            defaultQuality = p[Keys.QUALITY]?.let { runCatching { QualityPref.valueOf(it) }.getOrNull() }
                ?: QualityPref.HIGHEST,
            preferredAudioLanguage = p[Keys.AUDIO_LANG] ?: "ta",
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            audioOnly = p[Keys.AUDIO_ONLY] ?: false,
            playbackSpeed = p[Keys.SPEED] ?: 1.0f,
            sponsorBlockEnabled = p[Keys.SB_ENABLED] ?: false,
            sponsorCategories = p[Keys.SB_CATS]
                ?.split(',')?.filter { it.isNotBlank() }?.toSet()
                ?: SponsorCategory.DEFAULT,
        )
    }

    suspend fun setDefaultQuality(quality: QualityPref) {
        context.dataStore.edit { it[Keys.QUALITY] = quality.name }
    }

    suspend fun setPreferredAudioLanguage(code: String) {
        context.dataStore.edit { it[Keys.AUDIO_LANG] = code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setAudioOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUDIO_ONLY] = enabled }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.SPEED] = speed }
    }

    suspend fun setSponsorBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SB_ENABLED] = enabled }
    }

    suspend fun setSponsorCategories(categories: Set<String>) {
        context.dataStore.edit { it[Keys.SB_CATS] = categories.joinToString(",") }
    }
}
