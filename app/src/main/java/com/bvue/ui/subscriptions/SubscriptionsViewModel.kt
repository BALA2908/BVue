package com.bvue.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.LibraryRepository
import com.bvue.data.repository.YoutubeRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

sealed interface SubscriptionsUiState {
    data object Loading : SubscriptionsUiState
    data object Empty : SubscriptionsUiState
    data class Success(val videos: List<VideoItem>) : SubscriptionsUiState
    data class Error(val message: String) : SubscriptionsUiState
}

class SubscriptionsViewModel(
    private val youtube: YoutubeRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubscriptionsUiState>(SubscriptionsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    private var loaded = false

    fun loadIfNeeded() {
        if (loaded) return
        loaded = true
        load(isRefresh = false)
    }

    fun refresh() = load(isRefresh = true)

    private fun load(isRefresh: Boolean) {
        if (!isRefresh && _uiState.value !is SubscriptionsUiState.Success) {
            _uiState.value = SubscriptionsUiState.Loading
        }
        viewModelScope.launch {
            if (isRefresh) _refreshing.value = true
            try {
                val subs = library.subscriptionsSnapshot()
                if (subs.isEmpty()) {
                    _uiState.value = SubscriptionsUiState.Empty
                    return@launch
                }
                // Fan out, capped at 4 concurrent extractions; one dead channel can't kill the feed.
                val semaphore = Semaphore(4)
                val perChannel = coroutineScope {
                    subs.map { sub ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                runCatching { youtube.getChannel(sub.channelUrl).videos.take(10) }
                                    .getOrDefault(emptyList())
                            }
                        }
                    }.awaitAll()
                }
                // uploadDate is a humanized string (not sortable), so interleave round-robin:
                // newest of each channel first, then seconds, etc.
                val merged = roundRobin(perChannel).distinctBy { it.id }
                _uiState.value =
                    if (merged.isEmpty()) SubscriptionsUiState.Empty
                    else SubscriptionsUiState.Success(merged)
            } catch (t: Throwable) {
                if (_uiState.value !is SubscriptionsUiState.Success) {
                    _uiState.value = SubscriptionsUiState.Error(t.message ?: "Couldn't load subscriptions")
                }
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun roundRobin(lists: List<List<VideoItem>>): List<VideoItem> {
        val result = mutableListOf<VideoItem>()
        val maxSize = lists.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxSize) {
            for (list in lists) {
                if (i < list.size) result.add(list[i])
            }
        }
        return result
    }
}
