package com.bvue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String?,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val watchedAt: Long,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String?,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val addedAt: Long,
)

@Entity(tableName = "resume_positions")
data class ResumePositionEntity(
    @PrimaryKey val videoId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "playlist_videos", primaryKeys = ["playlistId", "videoId"])
data class PlaylistVideoEntity(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String?,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val addedAt: Long,
)

/** Recent search queries (v3). Query is the PK so re-running a search just refreshes its recency. */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long,
)

/** A channel the user follows locally — no Google login (v3). */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelUrl: String,
    val name: String,
    val avatarUrl: String?,
    val subscribedAt: Long,
)

/** Query projection: a playlist with its video count (not a table). */
data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val videoCount: Int,
)
