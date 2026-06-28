package com.bvue.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bvue.data.repository.LibraryRepository
import com.bvue.domain.model.VideoItem
import kotlinx.coroutines.launch

@Composable
fun AddToPlaylistDialog(item: VideoItem, lib: LibraryRepository, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val playlists by lib.playlists.collectAsStateWithLifecycle(initialValue = emptyList())
    val containing by lib.playlistIdsContaining(item.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to playlist") },
        text = {
            Column {
                playlists.forEach { playlist ->
                    val checked = playlist.id in containing
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    if (checked) lib.removeFromPlaylist(playlist.id, item.id)
                                    else lib.addToPlaylist(playlist.id, item)
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("${playlist.name}  (${playlist.videoCount})")
                    }
                }
                Spacer(Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("New playlist") },
                        singleLine = true,
                        modifier = Modifier.weightFill(),
                    )
                    IconButton(
                        onClick = {
                            val name = newName.trim()
                            if (name.isNotEmpty()) {
                                scope.launch {
                                    val id = lib.createPlaylist(name)
                                    lib.addToPlaylist(id, item)
                                }
                                newName = ""
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create playlist", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

private fun Modifier.weightFill(): Modifier = this.fillMaxWidth(0.8f)
