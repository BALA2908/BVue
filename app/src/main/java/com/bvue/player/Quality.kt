package com.bvue.player

import com.bvue.domain.model.QualityPref
import com.bvue.domain.model.StreamData
import com.bvue.domain.model.StreamOption

/** One selectable quality and the concrete stream(s) it plays. */
data class QualityChoice(
    val label: String,
    val video: StreamOption?,
    val audio: StreamOption?,
)

private fun heightOf(label: String): Int = label.takeWhile { it.isDigit() }.toIntOrNull() ?: 0

/**
 * Picks the audio track: prefer the user's language (e.g. Tamil "ta") when the video offers it,
 * otherwise the highest-bitrate track (typically the original).
 */
fun pickAudio(audioOnly: List<StreamOption>, preferredLang: String?): StreamOption? {
    if (audioOnly.isEmpty()) return null
    val preferred = if (!preferredLang.isNullOrBlank()) {
        audioOnly.filter { it.language == preferredLang }
    } else {
        emptyList()
    }
    return (preferred.ifEmpty { audioOnly }).maxByOrNull { it.bitrate }
}

/**
 * Builds the quality menu (rule 3): HD resolutions (>360p, up to 4K) as merged video + the chosen
 * audio track, ≤360p as the muxed single stream, plus an audio-only option.
 */
fun buildQualityChoices(data: StreamData, preferredAudioLang: String?): List<QualityChoice> {
    val audio = pickAudio(data.audioOnly, preferredAudioLang)
    val choices = mutableListOf<QualityChoice>()

    data.videoOnly
        .filter { it.height in 361..2160 }
        .groupBy { it.height }
        .mapValues { (_, group) -> group.maxByOrNull { it.bitrate } ?: group.first() }
        .entries
        .sortedByDescending { it.key }
        .forEach { (h, video) -> choices += QualityChoice("${h}p", video, audio) }

    val muxed = data.muxed.firstOrNull()
    if (muxed != null) {
        choices += QualityChoice(muxed.qualityLabel.ifBlank { "360p" }, muxed, null)
    } else {
        data.videoOnly.firstOrNull { it.height == 360 }?.let { choices += QualityChoice("360p", it, audio) }
    }

    if (audio != null) choices += QualityChoice("Audio only", null, audio)
    return choices
}

/** Picks a lightweight source for a Short (prefer muxed 360p, else the lowest available video). */
fun shortSource(data: StreamData, preferredAudioLang: String?): Pair<StreamOption?, StreamOption?> {
    val choices = buildQualityChoices(data, preferredAudioLang)
    val choice = choices.firstOrNull { it.label == "360p" }
        ?: choices.lastOrNull { it.video != null }
        ?: choices.firstOrNull()
    return Pair(choice?.video, choice?.audio)
}

/** Picks the initial quality from the user's default preference. */
fun defaultQuality(choices: List<QualityChoice>, pref: QualityPref): QualityChoice? {
    if (pref == QualityPref.AUDIO_ONLY) {
        return choices.firstOrNull { it.label == "Audio only" } ?: choices.firstOrNull()
    }
    val videoChoices = choices.filter { it.video != null }
    if (videoChoices.isEmpty()) return choices.firstOrNull()
    if (pref == QualityPref.HIGHEST) return videoChoices.maxByOrNull { heightOf(it.label) }

    val target = pref.targetHeight
    return videoChoices.firstOrNull { heightOf(it.label) == target }
        ?: videoChoices.filter { heightOf(it.label) <= target }.maxByOrNull { heightOf(it.label) }
        ?: videoChoices.minByOrNull { heightOf(it.label) }
}
