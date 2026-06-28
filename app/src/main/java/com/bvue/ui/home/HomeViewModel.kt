package com.bvue.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.YoutubeRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val videos: List<VideoItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repo: YoutubeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    private var loadedCategory: String? = null
    private var refreshTick = 0

    // YouTube's trending kiosk is static, so a "refresh" rotates through Tamil-biased seed queries
    // (the user prefers Tamil content) and shuffles, surfacing genuinely different videos each pull.
    private val allSeeds = listOf(
        "trending tamil", "tamil songs 2026", "tamil news today",
        "tamil cinema", "trending india", "latest tamil",
    )

    fun load(category: String, force: Boolean = false) {
        if (!force && category == loadedCategory && _uiState.value is HomeUiState.Success) return
        loadedCategory = category
        if (force) refreshTick++
        val isRefresh = force && _uiState.value is HomeUiState.Success
        if (!isRefresh) _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            if (isRefresh) _refreshing.value = true
            try {
                val videos = fetch(category, force)
                if (videos.isNotEmpty()) {
                    _uiState.value = HomeUiState.Success(videos)
                } else if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error("Nothing to show right now")
                }
            } catch (t: Throwable) {
                val fallback = runCatching {
                    repo.search(if (category == "All") "trending tamil" else category)
                }.getOrNull()
                if (!fallback.isNullOrEmpty()) {
                    _uiState.value = HomeUiState.Success(fallback)
                } else if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error(t.message ?: "Couldn't load the feed")
                }
            } finally {
                _refreshing.value = false
            }
        }
    }

    private suspend fun fetch(category: String, force: Boolean): List<VideoItem> {
        return if (category == "All") {
            if (force) {
                repo.search(allSeeds[refreshTick % allSeeds.size]).shuffled()
            } else {
                repo.getTrending().ifEmpty { repo.search("trending tamil") }
            }
        } else {
            val query = if (force) {
                when (refreshTick % 4) {
                    0 -> category
                    1 -> "latest $category"
                    2 -> "trending $category"
                    else -> "tamil $category"
                }
            } else {
                category
            }
            repo.search(query).let { if (force) it.shuffled() else it }
        }
    }
}
