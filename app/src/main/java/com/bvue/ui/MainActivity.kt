package com.bvue.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import com.bvue.BVueApplication

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* outcome ignored */ }

    private val pipController by lazy { PipController(this) }

    private val container get() = (application as BVueApplication).container

    // Toggles play/pause from the PiP window's RemoteAction button.
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_PIP_TOGGLE) {
                val player = container.exoPlayer
                if (player.isPlaying) player.pause() else player.play()
            }
        }
    }

    // Keeps the PiP action icon (play vs pause) in sync with the player.
    private val playingListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            pipController.setPlaying(isPlaying)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestNotificationPermissionIfNeeded()
        ContextCompat.registerReceiver(
            this,
            pipReceiver,
            IntentFilter(ACTION_PIP_TOGGLE),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        container.exoPlayer.addListener(playingListener)
        setContent {
            CompositionLocalProvider(LocalPipController provides pipController) {
                BVueRoot()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter PiP when leaving the app mid-playback (only the watch screen marks itself eligible).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            pipController.autoEnterEligible &&
            container.exoPlayer.isPlaying
        ) {
            pipController.enter()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipController.isInPip = isInPictureInPictureMode
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(pipReceiver) }
        container.exoPlayer.removeListener(playingListener)
        super.onDestroy()
    }

    // Rule 5: POST_NOTIFICATIONS is a runtime permission on Android 13+ (needed for the media notification).
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
