package com.bvue.data.repository

import com.bvue.data.local.BVueDatabase
import com.bvue.data.local.FavoriteEntity
import com.bvue.data.local.PlaylistEntity
import com.bvue.data.local.PlaylistVideoEntity
import com.bvue.data.local.PlaylistWithCount
import com.bvue.data.local.ResumePositionEntity
import com.bvue.data.local.SearchHistoryEntity
import com.bvue.data.local.SubscriptionEntity
import com.bvue.data.local.WatchHistoryEntity
import com.bvue.domain.model.VideoItem
import com.bvue.util.watchUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** All on-device user data: watch history, favorites, resume positions, playlists, search, subs. */
class LibraryRepository(private val db: BVueDatabase) {

    val watchHistory: Flow<List<VideoItem>> =
        db.watchHistoryDao().observe().map { list -> list.map { it.toVideoItem() } }

    val favorites: Flow<List<VideoItem>> =
        db.favoriteDao().observe().map { list -> list.map { it.toVideoItem() } }

    fun isFavorite(videoId: String): Flow<Boolean> = db.favoriteDao().isFavorite(videoId)

    suspend fun recordWatch(item: VideoItem) {
        db.watchHistoryDao().upsert(
            WatchHistoryEntity(
                videoId = item.id,
                title = item.title,
                uploader = item.uploader,
                uploaderUrl = item.uploaderUrl,
                durationSeconds = item.durationSeconds,
                thumbnailUrl = item.thumbnailUrl,
                watchedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setFavorite(item: VideoItem, favorite: Boolean) {
        if (favorite) {
            db.favoriteDao().add(
                FavoriteEntity(
                    videoId = item.id,
                    title = item.title,
                    uploader = item.uploader,
                    uploaderUrl = item.uploaderUrl,
                    durationSeconds = item.durationSeconds,
                    thumbnailUrl = item.thumbnailUrl,
                    addedAt = System.currentTimeMillis(),
                ),
            )
        } else {
            db.favoriteDao().remove(item.id)
        }
    }

    /** Returns a resume position to seek to, or null if none / video was nearly finished. */
    suspend fun getResumeMs(videoId: String): Long? {
        val r = db.resumePositionDao().get(videoId) ?: return null
        if (r.durationMs > 0 && r.positionMs > r.durationMs - 10_000) return null
        return r.positionMs.takeIf { it > 5_000 }
    }

    suspend fun saveResume(videoId: String, positionMs: Long, durationMs: Long) {
        if (positionMs < 5_000) return
        db.resumePositionDao().upsert(
            ResumePositionEntity(videoId, positionMs, durationMs, System.currentTimeMillis()),
        )
    }

    // --- Playlists ---
    val playlists: Flow<List<PlaylistWithCount>> = db.playlistDao().observePlaylists()

    fun playlistIdsContaining(videoId: String): Flow<List<Long>> =
        db.playlistDao().playlistIdsContaining(videoId)

    fun playlistVideos(playlistId: Long): Flow<List<VideoItem>> =
        db.playlistDao().observeVideos(playlistId).map { list -> list.map { it.toVideoItem() } }

    suspend fun createPlaylist(name: String): Long =
        db.playlistDao().createPlaylist(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    suspend fun addToPlaylist(playlistId: Long, item: VideoItem) =
        db.playlistDao().addVideo(
            PlaylistVideoEntity(
                playlistId = playlistId,
                videoId = item.id,
                title = item.title,
                uploader = item.uploader,
                uploaderUrl = item.uploaderUrl,
                durationSeconds = item.durationSeconds,
                thumbnailUrl = item.thumbnailUrl,
                addedAt = System.currentTimeMillis(),
            ),
        )

    suspend fun removeFromPlaylist(playlistId: Long, videoId: String) =
        db.playlistDao().removeVideo(playlistId, videoId)

    suspend fun playlistName(id: Long): String? = db.playlistDao().playlistName(id)

    // --- Search history ---
    val recentSearches: Flow<List<String>> =
        db.searchHistoryDao().observe().map { list -> list.map { it.query } }

    suspend fun recordSearch(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        db.searchHistoryDao().upsert(SearchHistoryEntity(q, System.currentTimeMillis()))
    }

    suspend fun deleteSearch(query: String) = db.searchHistoryDao().delete(query)

    suspend fun clearSearchHistory() = db.searchHistoryDao().clearAll()

    // --- Subscriptions (followed channels, no login) ---
    val subscriptions: Flow<List<SubscriptionEntity>> = db.subscriptionDao().observe()

    fun isSubscribed(channelUrl: String): Flow<Boolean> = db.subscriptionDao().isSubscribed(channelUrl)

    suspend fun subscribe(channelUrl: String, name: String, avatarUrl: String?) =
        db.subscriptionDao().add(
            SubscriptionEntity(channelUrl, name, avatarUrl, System.currentTimeMillis()),
        )

    suspend fun unsubscribe(channelUrl: String) = db.subscriptionDao().remove(channelUrl)

    suspend fun subscriptionsSnapshot(): List<SubscriptionEntity> = db.subscriptionDao().all()
}

private fun WatchHistoryEntity.toVideoItem() = VideoItem(
    id = videoId, url = watchUrl(videoId), title = title, uploader = uploader,
    uploaderUrl = uploaderUrl, durationSeconds = durationSeconds, viewCount = -1,
    uploadDate = null, thumbnailUrl = thumbnailUrl,
)

private fun FavoriteEntity.toVideoItem() = VideoItem(
    id = videoId, url = watchUrl(videoId), title = title, uploader = uploader,
    uploaderUrl = uploaderUrl, durationSeconds = durationSeconds, viewCount = -1,
    uploadDate = null, thumbnailUrl = thumbnailUrl,
)

private fun PlaylistVideoEntity.toVideoItem() = VideoItem(
    id = videoId, url = watchUrl(videoId), title = title, uploader = uploader,
    uploaderUrl = uploaderUrl, durationSeconds = durationSeconds, viewCount = -1,
    uploadDate = null, thumbnailUrl = thumbnailUrl,
)
