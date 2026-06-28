package com.bvue.ui.player

import android.content.ComponentName
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.bvue.BVueApplication
import com.bvue.domain.model.AppSettings
import com.bvue.domain.model.QualityPref
import com.bvue.domain.model.StreamData
import com.bvue.domain.model.VideoItem
import com.bvue.player.PlaybackService
import com.bvue.player.QualityChoice
import com.bvue.player.SleepTimerState
import com.bvue.player.buildQualityChoices
import com.bvue.player.defaultQuality
import com.bvue.ui.LocalPipController
import com.bvue.ui.anim.pressScale
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.SubscribeButton
import com.bvue.ui.components.VideoCard
import com.bvue.ui.components.formatCount
import com.bvue.ui.components.formatDuration
import com.bvue.ui.theme.BVueRed
import com.bvue.util.watchUrl
import com.bvue.util.youtubeThumb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WatchScreen(
    videoId: String,
    onBack: () -> Unit,
    onChannelClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as BVueApplication
    val scope = rememberCoroutineScope()
    val lib = app.container.libraryRepository
    val settingsRepo = app.container.settingsRepository
    val vm: PlayerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                PlayerViewModel(
                    app.container.youtubeRepository,
                    app.container.sponsorBlockRepository,
                    settingsRepo,
                )
            }
        },
    )
    LaunchedEffect(videoId) { vm.load(videoId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val segments by vm.segments.collectAsStateWithLifecycle()
    val settings by settingsRepo.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    val exoPlayer = app.container.exoPlayer
    val msFactory = app.container.mediaSourceFactory
    val sleepTimer = app.container.sleepTimerController
    val sleepState by sleepTimer.state.collectAsStateWithLifecycle()
    val pip = LocalPipController.current
    val isInPip = pip?.isInPip == true

    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        onDispose { MediaController.releaseFuture(future) }
    }

    // Rule 8: stream URLs expire / are IP-bound — on a playback error, re-extract and retry once.
    var retriedVideoId by remember { mutableStateOf<String?>(null) }
    DisposableEffect(exoPlayer, videoId) {
        val errorListener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (retriedVideoId != videoId) {
                    retriedVideoId = videoId
                    vm.load(videoId)
                }
            }
        }
        exoPlayer.addListener(errorListener)
        onDispose { exoPlayer.removeListener(errorListener) }
    }

    val data = (state as? PlayerUiState.Success)?.data
    var qualities by remember { mutableStateOf<List<QualityChoice>>(emptyList()) }
    var selected by remember { mutableStateOf<QualityChoice?>(null) }
    var appliedVideoId by remember { mutableStateOf<String?>(null) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data != null) {
            qualities = buildQualityChoices(data, settings.preferredAudioLanguage)
            val pref = if (settings.audioOnly) QualityPref.AUDIO_ONLY else settings.defaultQuality
            selected = defaultQuality(qualities, pref)
            lib.recordWatch(data.toVideoItem())
        }
    }
    LaunchedEffect(selected) {
        val sel = selected ?: return@LaunchedEffect
        val videoIdNow = data?.videoId ?: return@LaunchedEffect
        val sameVideo = appliedVideoId == videoIdNow
        val startPos = if (sameVideo) exoPlayer.currentPosition else (lib.getResumeMs(videoIdNow) ?: 0L)
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer.setMediaSource(msFactory.create(sel.video, sel.audio))
        exoPlayer.prepare()
        if (startPos > 0) exoPlayer.seekTo(startPos)
        exoPlayer.setPlaybackSpeed(settings.playbackSpeed)
        exoPlayer.playWhenReady = true
        appliedVideoId = videoIdNow
    }
    // Apply a mid-video speed change instantly.
    LaunchedEffect(settings.playbackSpeed) { exoPlayer.setPlaybackSpeed(settings.playbackSpeed) }
    LaunchedEffect(data?.videoId) {
        val vid = data?.videoId ?: return@LaunchedEffect
        while (true) {
            delay(5_000)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (pos > 0 && dur > 0) lib.saveResume(vid, pos, dur)
        }
    }

    // SponsorBlock: schedule a frame-accurate skip at each segment start. PlayerMessages don't carry
    // across a new media source, so this re-runs (re-schedules) whenever the prepared video/segments change.
    DisposableEffect(exoPlayer, segments, appliedVideoId, settings.sponsorBlockEnabled) {
        val handles = if (settings.sponsorBlockEnabled && segments.isNotEmpty() && appliedVideoId != null) {
            segments.map { seg ->
                exoPlayer.createMessage { _, _ ->
                    val pos = exoPlayer.currentPosition
                    if (pos in seg.startMs until seg.endMs) exoPlayer.seekTo(seg.endMs)
                }
                    .setLooper(exoPlayer.applicationLooper)
                    .setPosition(seg.startMs)
                    .setDeleteAfterDelivery(false)
                    .send()
            }
        } else {
            emptyList()
        }
        onDispose { handles.forEach { it.cancel() } }
    }

    // Auto-PiP eligibility: only while a real video is loaded on the watch screen.
    DisposableEffect(pip, data) {
        pip?.autoEnterEligible = (data != null)
        onDispose { pip?.autoEnterEligible = false }
    }

    val favFlow = remember(videoId) { lib.isFavorite(videoId) }
    val isFavorite by favFlow.collectAsStateWithLifecycle(initialValue = false)
    val uploaderUrl = data?.uploaderUrl
    val subFlow = remember(uploaderUrl) { lib.isSubscribed(uploaderUrl ?: "") }
    val isSubscribed by subFlow.collectAsStateWithLifecycle(initialValue = false)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (isInPip) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                .background(Color.Black),
        ) {
            if (data != null) {
                AndroidView(
                    factory = { ctx -> PlayerView(ctx).apply { useController = false; player = exoPlayer } },
                    modifier = Modifier.fillMaxSize(),
                )
                if (!isInPip) {
                    PlayerControls(
                        player = exoPlayer,
                        canPip = pip?.isSupported == true,
                        onEnterPip = { pip?.enter() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else if (state is PlayerUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            if (!isInPip) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        if (!isInPip) {
            when (val s = state) {
                is PlayerUiState.Restricted ->
                    MessageState(
                        "This video can't be played here (${s.restriction.name.lowercase().replace('_', ' ')}).",
                        Modifier.weight(1f),
                    )
                is PlayerUiState.Error -> MessageState(s.message, Modifier.weight(1f))
                is PlayerUiState.Success ->
                    LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                        item {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    text = s.data.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val meta = buildList {
                                    if (s.data.viewCount > 0) add("${formatCount(s.data.viewCount)} views")
                                    if (!s.data.uploadDate.isNullOrBlank()) add(s.data.uploadDate)
                                }.joinToString("  •  ")
                                if (meta.isNotEmpty()) {
                                    Text(
                                        text = meta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ActionPill(
                                        icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        label = if (isFavorite) "Saved" else "Save",
                                        tint = if (isFavorite) BVueRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        onClick = { scope.launch { lib.setFavorite(s.data.toVideoItem(), !isFavorite) } },
                                    )
                                    SpeedPill(
                                        speed = settings.playbackSpeed,
                                        onSelect = { sp -> scope.launch { settingsRepo.setPlaybackSpeed(sp) } },
                                    )
                                    ActionPill(
                                        icon = Icons.Filled.Bedtime,
                                        label = sleepLabel(sleepState),
                                        tint = if (sleepState is SleepTimerState.Idle) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            BVueRed
                                        },
                                        onClick = { showSleepDialog = true },
                                    )
                                    ActionPill(
                                        icon = Icons.Outlined.PlaylistAdd,
                                        label = "Save to playlist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        onClick = { showPlaylistDialog = true },
                                    )
                                }
                                if (s.data.uploader.isNotBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = s.data.uploader,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = s.data.uploaderUrl != null) {
                                                    s.data.uploaderUrl?.let(onChannelClick)
                                                },
                                        )
                                        if (s.data.uploaderUrl != null) {
                                            Spacer(Modifier.width(12.dp))
                                            SubscribeButton(
                                                subscribed = isSubscribed,
                                                onClick = {
                                                    val url = s.data.uploaderUrl
                                                    scope.launch {
                                                        if (isSubscribed) {
                                                            lib.unsubscribe(url)
                                                        } else {
                                                            lib.subscribe(url, s.data.uploader, null)
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                QualitySelector(qualities = qualities, selected = selected, onSelect = { selected = it })
                            }
                        }
                        items(s.data.relatedItems) { related ->
                            VideoCard(item = related, onClick = { onVideoClick(related.id) })
                        }
                    }
                PlayerUiState.Loading -> Spacer(Modifier.weight(1f))
            }
        }
    }

    if (showPlaylistDialog && data != null) {
        AddToPlaylistDialog(item = data.toVideoItem(), lib = lib, onDismiss = { showPlaylistDialog = false })
    }
    if (showSleepDialog) {
        SleepTimerDialog(
            onPick = { minutes ->
                when {
                    minutes == 0 -> sleepTimer.cancel()
                    minutes < 0 -> sleepTimer.startEndOfVideo()
                    else -> sleepTimer.startMinutes(minutes)
                }
                showSleepDialog = false
            },
            onDismiss = { showSleepDialog = false },
        )
    }
}

private fun sleepLabel(state: SleepTimerState): String = when (state) {
    is SleepTimerState.Idle -> "Sleep timer"
    is SleepTimerState.UntilEndOfVideo -> "Sleep: end"
    is SleepTimerState.Running -> "Sleep ${formatDuration((state.remainingMs / 1000).coerceAtLeast(0))}"
}

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()

private data class SeekFlash(val forward: Boolean, val id: Int)

@Composable
private fun PlayerControls(
    player: Player,
    canPip: Boolean,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(true) }
    var playing by remember { mutableStateOf(player.isPlaying) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var seekFlash by remember { mutableStateOf<SeekFlash?>(null) }
    var seekCounter by remember { mutableIntStateOf(0) }
    val flashAlpha = remember { Animatable(0f) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
        }
        player.addListener(listener)
        playing = player.isPlaying
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0L)
            delay(500)
        }
    }
    LaunchedEffect(visible, playing) {
        if (visible && playing) {
            delay(3_000)
            visible = false
        }
    }
    // Double-tap seek feedback: pulse in then fade out, independent of the controls' visibility.
    LaunchedEffect(seekFlash) {
        if (seekFlash != null) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, tween(650))
            seekFlash = null
        }
    }

    Box(
        modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { visible = !visible },
                onDoubleTap = { offset ->
                    val forward = offset.x >= size.width / 2f
                    val target = if (forward) {
                        player.currentPosition + 10_000
                    } else {
                        (player.currentPosition - 10_000).coerceAtLeast(0L)
                    }
                    player.seekTo(target)
                    seekCounter += 1
                    seekFlash = SeekFlash(forward, seekCounter)
                    visible = true
                },
            )
        },
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)))
                if (canPip) {
                    IconButton(onClick = onEnterPip, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            Icons.Filled.PictureInPictureAlt,
                            contentDescription = "Picture-in-picture",
                            tint = Color.White,
                        )
                    }
                }
                IconButton(
                    onClick = { if (player.isPlaying) player.pause() else player.play() },
                    modifier = Modifier.align(Alignment.Center).size(64.dp),
                ) {
                    AnimatedContent(
                        targetState = playing,
                        transitionSpec = {
                            (scaleIn(tween(160)) + fadeIn(tween(160))) togetherWith
                                (scaleOut(tween(160)) + fadeOut(tween(160)))
                        },
                        label = "playPause",
                    ) { isPlaying ->
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(formatDuration(position / 1000), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                        onValueChange = { if (duration > 0) player.seekTo((it * duration).toLong()) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = BVueRed, activeTrackColor = BVueRed),
                    )
                    Text(formatDuration(duration / 1000), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        seekFlash?.let { flash ->
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(if (flash.forward) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = 44.dp)
                        .graphicsLayer { alpha = flashAlpha.value },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = if (flash.forward) Icons.Filled.Forward10 else Icons.Filled.Replay10,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                    Text("10 seconds", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun StreamData.toVideoItem() = VideoItem(
    id = videoId,
    url = watchUrl(videoId),
    title = title,
    uploader = uploader,
    uploaderUrl = uploaderUrl,
    durationSeconds = durationSeconds,
    viewCount = viewCount,
    uploadDate = uploadDate,
    thumbnailUrl = youtubeThumb(videoId),
)

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pressScale(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SpeedPill(speed: Float, onSelect: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ActionPill(
            icon = Icons.Filled.Speed,
            label = "${formatSpeed(speed)}x",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { option ->
                DropdownMenuItem(
                    text = { Text("${formatSpeed(option)}x") },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SleepTimerDialog(onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        15 to "15 minutes",
        30 to "30 minutes",
        45 to "45 minutes",
        60 to "60 minutes",
        -1 to "End of video",
        0 to "Off",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep timer") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(value) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun QualitySelector(
    qualities: List<QualityChoice>,
    selected: QualityChoice?,
    onSelect: (QualityChoice) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("Quality: ${selected?.label ?: "—"}") },
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            qualities.forEach { quality ->
                DropdownMenuItem(
                    text = { Text(quality.label) },
                    onClick = {
                        onSelect(quality)
                        expanded = false
                    },
                )
            }
        }
    }
}
