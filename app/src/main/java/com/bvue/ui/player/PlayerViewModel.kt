package com.bvue.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.SponsorBlockRepository
import com.bvue.data.repository.YoutubeRepository
import com.bvue.data.settings.SettingsRepository
import com.bvue.domain.model.ContentRestriction
import com.bvue.domain.model.RestrictedContentException
import com.bvue.domain.model.SponsorSegment
import com.bvue.domain.model.StreamData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Success(val data: StreamData) : PlayerUiState
    data class Restricted(val restriction: ContentRestriction) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}

class PlayerViewModel(
    private val repo: YoutubeRepository,
    private val sponsorBlock: SponsorBlockRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _segments = MutableStateFlow<List<SponsorSegment>>(emptyList())
    val segments = _segments.asStateFlow()

    fun load(videoId: String) {
        _uiState.value = PlayerUiState.Loading
        _segments.value = emptyList()
        viewModelScope.launch {
            _uiState.value = try {
                val url = "https://www.youtube.com/watch?v=$videoId"
                val data = repo.resolveStreams(url)
                fetchSegments(videoId)
                PlayerUiState.Success(data)
            } catch (e: RestrictedContentException) {
                PlayerUiState.Restricted(e.restriction)
            } catch (t: Throwable) {
                PlayerUiState.Error(t.message ?: "Couldn't load this video")
            }
        }
    }

    /** SponsorBlock fetch is fail-open and gated by the user's setting; runs off the main load path. */
    private fun fetchSegments(videoId: String) {
        viewModelScope.launch {
            val settings = settingsRepo.settings.first()
            _segments.value = if (settings.sponsorBlockEnabled) {
                sponsorBlock.fetchSegments(videoId, settings.sponsorCategories)
            } else {
                emptyList()
            }
        }
    }
}
