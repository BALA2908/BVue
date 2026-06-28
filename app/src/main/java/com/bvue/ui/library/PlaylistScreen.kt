package com.bvue.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bvue.BVueApplication
import com.bvue.data.repository.LibraryRepository
import com.bvue.domain.model.VideoItem
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.VideoCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(repo: LibraryRepository, playlistId: Long) : ViewModel() {
    private val _name = MutableStateFlow("Playlist")
    val name: StateFlow<String> = _name.asStateFlow()
    val videos: StateFlow<List<VideoItem>> =
        repo.playlistVideos(playlistId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { _name.value = repo.playlistName(playlistId) ?: "Playlist" }
    }
}

@Composable
fun PlaylistScreen(playlistId: Long, onBack: () -> Unit, onVideoClick: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: PlaylistViewModel = viewModel(
        factory = viewModelFactory { initializer { PlaylistViewModel(app.container.libraryRepository, playlistId) } },
    )
    val name by vm.name.collectAsStateWithLifecycle()
    val videos by vm.videos.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (videos.isEmpty()) {
            MessageState("This playlist is empty")
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(videos) { video ->
                    VideoCard(item = video, onClick = { onVideoClick(video.id) })
                }
            }
        }
    }
}
