package com.bvue.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PlaylistPlay
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bvue.BVueApplication
import com.bvue.data.local.PlaylistWithCount
import com.bvue.ui.components.MessageState
import com.bvue.ui.components.VideoCard

@Composable
fun LibraryScreen(
    onVideoClick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: LibraryViewModel = viewModel(
        factory = viewModelFactory { initializer { LibraryViewModel(app.container.libraryRepository) } },
    )
    val history by vm.history.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val playlists by vm.playlists.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("You", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (history.isEmpty() && favorites.isEmpty() && playlists.isEmpty()) {
            MessageState("Videos you watch, save, and add to playlists will appear here")
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                if (playlists.isNotEmpty()) {
                    item { SectionHeader("Playlists") }
                    items(playlists) { playlist ->
                        PlaylistRow(playlist, onClick = { onPlaylistClick(playlist.id) })
                    }
                }
                if (history.isNotEmpty()) {
                    item { SectionHeader("History") }
                    items(history.take(30)) { video ->
                        VideoCard(item = video, onClick = { onVideoClick(video.id) })
                    }
                }
                if (favorites.isNotEmpty()) {
                    item { SectionHeader("Saved") }
                    items(favorites) { video ->
                        VideoCard(item = video, onClick = { onVideoClick(video.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: PlaylistWithCount, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.PlaylistPlay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(playlist.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "${playlist.videoCount} videos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
    )
}
