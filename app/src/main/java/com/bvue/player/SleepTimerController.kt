package com.bvue.player

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SleepTimerState {
    data object Idle : SleepTimerState
    data class Running(val remainingMs: Long) : SleepTimerState
    data object UntilEndOfVideo : SleepTimerState
}

/**
 * App-scoped sleep timer. Pauses the single shared player after N minutes or at end-of-video.
 * Lives in [com.bvue.di.AppContainer] (not Compose) so the countdown keeps running when the user
 * leaves the watch screen or backgrounds the app. All calls happen on the main thread, same as the
 * player itself.
 */
class SleepTimerController(private val player: Player) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<SleepTimerState>(SleepTimerState.Idle)
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    private var job: Job? = null
    private var endListener: Player.Listener? = null

    fun startMinutes(minutes: Int) {
        cancel()
        var remaining = minutes * 60_000L
        _state.value = SleepTimerState.Running(remaining)
        job = scope.launch {
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1_000
                _state.value = SleepTimerState.Running(remaining.coerceAtLeast(0L))
            }
            player.pause()
            _state.value = SleepTimerState.Idle
        }
    }

    fun startEndOfVideo() {
        cancel()
        _state.value = SleepTimerState.UntilEndOfVideo
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    player.pause()
                    cancel()
                }
            }
        }
        endListener = listener
        player.addListener(listener)
    }

    fun cancel() {
        job?.cancel()
        job = null
        endListener?.let { player.removeListener(it) }
        endListener = null
        _state.value = SleepTimerState.Idle
    }
}
