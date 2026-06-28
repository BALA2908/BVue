package com.bvue.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** User's default video-quality preference. HIGHEST picks the top available (capped at 4K). */
enum class QualityPref(val label: String, val targetHeight: Int) {
    HIGHEST("Highest (up to 4K)", 2160),
    P2160("2160p (4K)", 2160),
    P1440("1440p", 1440),
    P1080("1080p", 1080),
    P720("720p", 720),
    P480("480p", 480),
    P360("360p", 360),
    AUDIO_ONLY("Audio only", 0),
}

/** A few common audio-track languages to choose from (ISO 639-1 codes). */
enum class AudioLanguage(val code: String, val label: String) {
    TAMIL("ta", "Tamil"),
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi"),
    TELUGU("te", "Telugu"),
    DEFAULT("", "Original / Default"),
    ;

    companion object {
        fun fromCode(code: String): AudioLanguage = entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

data class AppSettings(
    val defaultQuality: QualityPref = QualityPref.P1080,
    val preferredAudioLanguage: String = AudioLanguage.TAMIL.code, // "ta"
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val audioOnly: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val sponsorBlockEnabled: Boolean = false,
    val sponsorCategories: Set<String> = SponsorCategory.DEFAULT,
)
