package com.bvue.domain.model

/** A video as shown in search results, trending, channels, and feeds. */
data class VideoItem(
    val id: String,
    val url: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String?,
    val durationSeconds: Long,
    val viewCount: Long,
    val uploadDate: String?,
    val thumbnailUrl: String?,
)

/** A channel and (the first page of) its videos. */
data class ChannelData(
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long,
    val videos: List<VideoItem>,
)

enum class StreamKind { MUXED, VIDEO_ONLY, AUDIO_ONLY }

/** One selectable stream — a direct URL we can hand to ExoPlayer. */
data class StreamOption(
    val url: String,
    val kind: StreamKind,
    val qualityLabel: String,
    val isVideo: Boolean,
    val height: Int = 0,
    val bitrate: Int = 0,
    val language: String? = null,
    val trackName: String? = null,
)

/**
 * All streams resolved for a video (rule 3): muxed (≤360p), adaptive video-only (HD up to 4K),
 * and adaptive audio-only (possibly multiple language tracks).
 */
data class StreamData(
    val videoId: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String?,
    val viewCount: Long,
    val uploadDate: String?,
    val durationSeconds: Long,
    val relatedItems: List<VideoItem>,
    val muxed: List<StreamOption>,
    val videoOnly: List<StreamOption>,
    val audioOnly: List<StreamOption>,
)

enum class ContentRestriction { AGE_RESTRICTED, MEMBERS_ONLY, PAID, GEO, DRM, UNAVAILABLE }

/** Thrown when a video can't be played (rule 9); surfaced to the user as a friendly message. */
class RestrictedContentException(
    val restriction: ContentRestriction,
    message: String? = null,
) : Exception(message)
