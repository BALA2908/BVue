package com.bvue.ui.shorts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.bvue.BVueApplication
import com.bvue.domain.model.AppSettings
import com.bvue.domain.model.VideoItem
import com.bvue.player.shortSource
import com.bvue.ui.anim.pressScale
import com.bvue.ui.components.AnimatedToggleIcon
import com.bvue.ui.theme.BVueRed

@Composable
fun ShortsScreen() {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: ShortsViewModel = viewModel(
        factory = viewModelFactory { initializer { ShortsViewModel(app.container.youtubeRepository) } },
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val exoPlayer = app.container.exoPlayer
    val repo = app.container.youtubeRepository
    val msFactory = app.container.mediaSourceFactory
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when (val s = state) {
            ShortsUiState.Loading ->
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            is ShortsUiState.Error ->
                Text(
                    text = s.message,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
            is ShortsUiState.Success -> {
                val feed = s.shorts
                val pagerState = rememberPagerState(pageCount = { feed.size })

                DisposableEffect(Unit) {
                    onDispose {
                        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                        exoPlayer.pause()
                    }
                }
                LaunchedEffect(pagerState.settledPage, feed) {
                    val item = feed.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
                    runCatching {
                        val data = repo.resolveStreams(item.url)
                        val (video, audio) = shortSource(data, settings.preferredAudioLanguage)
                        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                        exoPlayer.setMediaSource(msFactory.create(video, audio))
                        exoPlayer.prepare()
                        exoPlayer.setPlaybackSpeed(settings.playbackSpeed)
                        exoPlayer.playWhenReady = true
                    }
                }

                VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    ShortPage(
                        item = feed[page],
                        isActive = page == pagerState.settledPage,
                        player = exoPlayer,
                    )
                }
            }
        }

        // Pull-to-refresh fights the vertical pager, so Shorts uses a simple refresh button.
        IconButton(
            onClick = { vm.refresh() },
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(4.dp),
        ) {
            if (refreshing) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ShortPage(item: VideoItem, isActive: Boolean, player: ExoPlayer) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (isActive) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        this.player = player
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setKeepContentOnPlayerReset(true)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = interaction, indication = null) {
                        if (player.isPlaying) player.pause() else player.play()
                    },
            )
        } else {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 24.dp, end = 64.dp),
        ) {
            if (item.uploader.isNotBlank()) {
                Text(
                    text = "@${item.uploader}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = item.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            RailEntrance(isActive, 0) { LikeAction() }
            RailEntrance(isActive, 1) { ActionIcon(Icons.Outlined.ThumbDown, "Dislike") }
            RailEntrance(isActive, 2) { ActionIcon(Icons.Outlined.Comment, "") }
            RailEntrance(isActive, 3) { ActionIcon(Icons.Outlined.Share, "Share") }
        }
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(30.dp))
        if (label.isNotEmpty()) {
            Text(text = label, color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LikeAction() {
    var liked by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pressScale { liked = !liked },
    ) {
        AnimatedToggleIcon(
            active = liked,
            activeIcon = Icons.Filled.ThumbUp,
            inactiveIcon = Icons.Outlined.ThumbUp,
            activeTint = BVueRed,
            inactiveTint = Color.White,
            contentDescription = "Like",
            size = 30.dp,
        )
        Text(text = "Like", color = Color.White, fontSize = 11.sp)
    }
}

/** Slides each action-rail item in (staggered) once its Short settles into view. */
@Composable
private fun RailEntrance(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220, delayMillis = index * 45)) +
            slideInHorizontally(tween(220, delayMillis = index * 45)) { it / 2 },
        exit = fadeOut(tween(120)),
    ) { content() }
}
