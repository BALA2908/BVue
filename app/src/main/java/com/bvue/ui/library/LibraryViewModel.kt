package com.bvue.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvue.data.local.PlaylistWithCount
import com.bvue.data.repository.LibraryRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(repo: LibraryRepository) : ViewModel() {
    val history: StateFlow<List<VideoItem>> =
        repo.watchHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val favorites: StateFlow<List<VideoItem>> =
        repo.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val playlists: StateFlow<List<PlaylistWithCount>> =
        repo.playlists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
