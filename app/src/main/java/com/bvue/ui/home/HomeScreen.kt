package com.bvue.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bvue.BVueApplication
import com.bvue.ui.anim.feedEntrance
import com.bvue.ui.anim.rememberPlayedFeedKeys
import com.bvue.ui.components.ChipRow
import com.bvue.ui.components.FeedSkeleton
import com.bvue.ui.components.GradientRefreshIndicator
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.VideoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onVideoClick: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(app.container.youtubeRepository) } },
    )
    val chips = remember { listOf("All", "Love", "Melody", "Romantic", "Hits", "90s", "Ilaiyaraaja", "A.R. Rahman") }
    var selected by rememberSaveable { mutableStateOf("All") }
    LaunchedEffect(selected) { vm.load(selected) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val ptrState = rememberPullToRefreshState()
    val played = rememberPlayedFeedKeys()

    Column(Modifier.fillMaxSize()) {
        ChipRow(chips = chips, selected = selected, onSelect = { selected = it })
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { vm.load(selected, force = true) },
            modifier = Modifier.fillMaxSize(),
            state = ptrState,
            indicator = {
                GradientRefreshIndicator(ptrState, refreshing, Modifier.align(Alignment.TopCenter))
            },
        ) {
            when (val s = state) {
                HomeUiState.Loading -> FeedSkeleton()
                is HomeUiState.Error -> MessageState(s.message)
                is HomeUiState.Success -> LazyColumn(Modifier.fillMaxSize()) {
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
