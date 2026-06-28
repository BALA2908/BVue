package com.bvue.player

import androidx.media3.common.MediaItem
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bvue.domain.model.StreamOption
import com.bvue.util.UserAgents
import okhttp3.OkHttpClient

/**
 * Builds ExoPlayer media sources from resolved streams (rule 3). Uses the SAME OkHttp client + a
 * desktop User-Agent as extraction, since googlevideo CDNs are picky about consistency.
 */
class PlayerMediaSourceFactory(okHttpClient: OkHttpClient) {

    private val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        .setUserAgent(UserAgents.DESKTOP_CHROME)

    private fun progressive(url: String): MediaSource =
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))

    /**
     * muxed (≤360p): a single combined stream. HD: a video-only stream merged with an audio-only
     * stream via [MergingMediaSource] so they stay in sync. Audio-only: just the audio stream.
     */
    fun create(video: StreamOption?, audio: StreamOption?): MediaSource = when {
        video != null && audio != null ->
            MergingMediaSource(true, progressive(video.url), progressive(audio.url))
        video != null -> progressive(video.url)
        audio != null -> progressive(audio.url)
        else -> error("No stream available to play")
    }
}
