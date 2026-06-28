package com.bvue.ui.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.YoutubeRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ShortsUiState {
    data object Loading : ShortsUiState
    data class Success(val shorts: List<VideoItem>) : ShortsUiState
    data class Error(val message: String) : ShortsUiState
}

class ShortsViewModel(private val repo: YoutubeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    private var tick = 0
    private val queries = listOf(
        "tamil shorts", "tamil comedy shorts", "tamil status", "trending shorts india",
    )

    init { load() }

    fun load() {
        _uiState.value = ShortsUiState.Loading
        viewModelScope.launch { fetch() }
    }

    /** Pull a fresh batch without clearing the current feed (keeps the pager visible). */
    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            try {
                fetch()
            } finally {
                _refreshing.value = false
            }
        }
    }

    private suspend fun fetch() {
        val query = queries[tick % queries.size]
        tick++
        _uiState.value = try {
            val shorts = repo.getShortsFeed(query)
            when {
                shorts.isNotEmpty() -> ShortsUiState.Success(shorts.shuffled())
                _uiState.value is ShortsUiState.Success -> _uiState.value // keep current on empty refresh
                else -> ShortsUiState.Error("No Shorts found")
            }
        } catch (t: Throwable) {
            if (_uiState.value is ShortsUiState.Success) _uiState.value
            else ShortsUiState.Error(t.message ?: "Couldn't load Shorts")
        }
    }
}
