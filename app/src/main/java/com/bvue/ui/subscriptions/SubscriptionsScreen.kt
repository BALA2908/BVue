package com.bvue.ui.subscriptions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bvue.BVueApplication
import com.bvue.ui.anim.feedEntrance
import com.bvue.ui.anim.rememberPlayedFeedKeys
import com.bvue.ui.components.FeedSkeleton
import com.bvue.ui.components.GradientRefreshIndicator
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.VideoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(onVideoClick: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: SubscriptionsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SubscriptionsViewModel(app.container.youtubeRepository, app.container.libraryRepository) }
        },
    )
    LaunchedEffect(Unit) { vm.loadIfNeeded() }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val ptrState = rememberPullToRefreshState()
    val played = rememberPlayedFeedKeys()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Subscriptions", style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.fillMaxSize(),
            state = ptrState,
            indicator = {
                GradientRefreshIndicator(ptrState, refreshing, Modifier.align(Alignment.TopCenter))
            },
        ) {
            when (val s = state) {
                SubscriptionsUiState.Loading -> FeedSkeleton()
                SubscriptionsUiState.Empty ->
                    MessageState("Subscribe to channels and their latest videos will appear here")
                is SubscriptionsUiState.Error -> MessageState(s.message)
                is SubscriptionsUiState.Success -> LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(s.videos, key = { _, item -> item.id }) { index, video ->
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
}
