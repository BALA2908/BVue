package com.bvue.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.bvue.BVueApplication

/**
 * Media3 MediaSessionService (rule 4) hosting the app-scoped ExoPlayer. Provides the MediaSession
 * that powers the media notification and lock-screen / Bluetooth / headset controls, and keeps
 * playback alive in the background as a foreground service (foregroundServiceType=mediaPlayback).
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val container = (application as BVueApplication).container
        mediaSession = MediaSession.Builder(this, container.exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        // If the user swipes the app away while paused, stop the service (and its notification).
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        // Release the session only — the ExoPlayer is app-scoped (owned by AppContainer).
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
