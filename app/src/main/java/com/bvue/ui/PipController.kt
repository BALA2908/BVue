package com.bvue.ui

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.bvue.BVueApplication

/** Broadcast action for the PiP window's play/pause button. */
const val ACTION_PIP_TOGGLE = "com.bvue.PIP_TOGGLE_PLAY"

/** Lets the watch screen drive Picture-in-Picture without holding an Activity reference itself. */
val LocalPipController = staticCompositionLocalOf<PipController?> { null }

class PipController(private val activity: Activity) {

    /** True while the app is showing in a PiP window (drives hiding the chrome on the watch screen). */
    var isInPip by mutableStateOf(false)

    /** Only the watch screen sets this true, so we never auto-PiP from Shorts/home/etc. */
    var autoEnterEligible by mutableStateOf(false)

    private var currentlyPlaying by mutableStateOf(false)

    val isSupported: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    fun setPlaying(isPlaying: Boolean) {
        currentlyPlaying = isPlaying
        if (isInPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { activity.setPictureInPictureParams(buildParams()) }
        }
    }

    fun enter() {
        if (!isSupported || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        runCatching { activity.enterPictureInPictureMode(buildParams()) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildParams(): PictureInPictureParams {
        val player = (activity.application as BVueApplication).container.exoPlayer
        val size = player.videoSize
        val ratio = if (size.width > 0 && size.height > 0) {
            val r = size.width.toFloat() / size.height.toFloat()
            if (r in 0.42f..2.39f) Rational(size.width, size.height) else Rational(16, 9)
        } else {
            Rational(16, 9)
        }

        val toggle = RemoteAction(
            Icon.createWithResource(
                activity,
                if (currentlyPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            ),
            if (currentlyPlaying) "Pause" else "Play",
            if (currentlyPlaying) "Pause" else "Play",
            PendingIntent.getBroadcast(
                activity,
                0,
                Intent(ACTION_PIP_TOGGLE).setPackage(activity.packageName),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        return PictureInPictureParams.Builder()
            .setAspectRatio(ratio)
            .setActions(listOf(toggle))
            .build()
    }
}
