package com.bvue.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.repository.LibraryRepository
import com.bvue.data.repository.YoutubeRepository
import com.bvue.domain.model.ChannelData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChannelUiState {
    data object Loading : ChannelUiState
    data class Success(val channel: ChannelData) : ChannelUiState
    data class Error(val message: String) : ChannelUiState
}

class ChannelViewModel(
    private val repo: YoutubeRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChannelUiState>(ChannelUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load(channelUrl: String) {
        _uiState.value = ChannelUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ChannelUiState.Success(repo.getChannel(channelUrl))
            } catch (t: Throwable) {
                ChannelUiState.Error(t.message ?: "Couldn't load channel")
            }
        }
    }

    fun isSubscribed(channelUrl: String): Flow<Boolean> = library.isSubscribed(channelUrl)

    fun toggleSubscribe(channelUrl: String, channel: ChannelData, currentlySubscribed: Boolean) =
        viewModelScope.launch {
            if (currentlySubscribed) {
                library.unsubscribe(channelUrl)
            } else {
                library.subscribe(channelUrl, channel.name, channel.avatarUrl)
            }
        }
}
