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

    // The user loves beautiful Tamil songs / love songs and dislikes cricket — so the "All" feed
    // rotates through these searches instead of YouTube trending (which surfaced cricket/news).
    private val songSeeds = listOf(
        "tamil love songs",
        "tamil melody songs",
        "tamil romantic songs",
        "best tamil love songs",
        "tamil 90s love songs",
        "ilaiyaraaja melody songs",
        "ar rahman tamil love songs",
        "tamil hit songs",
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
                val videos = repo.search(queryFor(category)).let { if (force) it.shuffled() else it }
                if (videos.isNotEmpty()) {
                    _uiState.value = HomeUiState.Success(videos)
                } else if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error("Nothing to show right now")
                }
            } catch (t: Throwable) {
                val fallback = runCatching { repo.search("tamil love songs") }.getOrNull()
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

    private fun queryFor(category: String): String = when (category) {
        "All" -> songSeeds[refreshTick % songSeeds.size]
        "Love" -> "tamil love songs"
        "Melody" -> "tamil melody songs"
        "Romantic" -> "tamil romantic songs"
        "Hits" -> "tamil hit songs"
        "90s" -> "tamil 90s hit songs"
        "Ilaiyaraaja" -> "ilaiyaraaja tamil hit songs"
        "A.R. Rahman" -> "ar rahman tamil songs"
        else -> "tamil $category songs"
    }
}
