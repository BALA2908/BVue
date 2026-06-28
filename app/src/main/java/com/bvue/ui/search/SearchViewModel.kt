package com.bvue.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.LibraryRepository
import com.bvue.data.repository.YoutubeRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<VideoItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val repo: YoutubeRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<String>> =
        library.recentSearches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun search(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val results = repo.search(q)
                library.recordSearch(q) // record only successful searches
                SearchUiState.Success(results)
            } catch (t: Throwable) {
                SearchUiState.Error(t.message ?: "Search failed")
            }
        }
    }

    fun deleteRecent(query: String) = viewModelScope.launch { library.deleteSearch(query) }
    fun clearRecents() = viewModelScope.launch { library.clearSearchHistory() }
}
