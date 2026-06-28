package com.bvue.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.bvue.BVueApplication
import com.bvue.domain.model.ChannelData
import com.bvue.ui.anim.feedEntrance
import com.bvue.ui.anim.rememberPlayedFeedKeys
import com.bvue.ui.components.FeedSkeleton
import com.bvue.ui.components.LoadingState
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.SubscribeButton
import com.bvue.ui.components.VideoCard
import com.bvue.ui.components.formatCount

@Composable
fun ChannelScreen(
    channelUrl: String,
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: ChannelViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ChannelViewModel(app.container.youtubeRepository, app.container.libraryRepository) }
        },
    )
    LaunchedEffect(channelUrl) { vm.load(channelUrl) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val subscribed by vm.isSubscribed(channelUrl).collectAsStateWithLifecycle(initialValue = false)
    val played = rememberPlayedFeedKeys()
    val title = (state as? ChannelUiState.Success)?.channel?.name ?: "Channel"

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (val s = state) {
            ChannelUiState.Loading -> FeedSkeleton()
            is ChannelUiState.Error -> MessageState(s.message)
            is ChannelUiState.Success -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    ChannelHeader(
                        channel = s.channel,
                        subscribed = subscribed,
                        onToggleSubscribe = { vm.toggleSubscribe(channelUrl, s.channel, subscribed) },
                    )
                }
                itemsIndexed(s.channel.videos, key = { _, item -> item.id }) { index, video ->
                    VideoCard(
                        item = video,
                        modifier = Modifier.feedEntrance(video.id, index, played),
                        onClick = { onVideoClick(video.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelHeader(
    channel: ChannelData,
    subscribed: Boolean,
    onToggleSubscribe: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (channel.avatarUrl != null) {
                AsyncImage(
                    model = channel.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                )
            } else {
                Text(
                    text = channel.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (channel.subscriberCount >= 0) {
                Text(
                    text = "${formatCount(channel.subscriberCount)} subscribers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SubscribeButton(subscribed = subscribed, onClick = onToggleSubscribe)
    }
}
