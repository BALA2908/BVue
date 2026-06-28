package com.bvue.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 200")
    fun observe(): Flow<List<WatchHistoryEntity>>

    @Upsert
    suspend fun upsert(entity: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observe(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE videoId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Upsert
    suspend fun add(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE videoId = :id")
    suspend fun remove(id: String)
}

@Dao
interface ResumePositionDao {
    @Query("SELECT * FROM resume_positions WHERE videoId = :id")
    suspend fun get(id: String): ResumePositionEntity?

    @Upsert
    suspend fun upsert(entity: ResumePositionEntity)

    @Query("DELETE FROM resume_positions WHERE videoId = :id")
    suspend fun clear(id: String)
}

@Dao
interface PlaylistDao {
    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query(
        "SELECT p.id AS id, p.name AS name, COUNT(v.videoId) AS videoCount " +
            "FROM playlists p LEFT JOIN playlist_videos v ON p.id = v.playlistId " +
            "GROUP BY p.id ORDER BY p.createdAt DESC",
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Upsert
    suspend fun addVideo(entity: PlaylistVideoEntity)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideo(playlistId: Long, videoId: String)

    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY addedAt DESC")
    fun observeVideos(playlistId: Long): Flow<List<PlaylistVideoEntity>>

    @Query("SELECT playlistId FROM playlist_videos WHERE videoId = :videoId")
    fun playlistIdsContaining(videoId: String): Flow<List<Long>>

    @Query("SELECT name FROM playlists WHERE id = :id")
    suspend fun playlistName(id: Long): String?
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 12")
    fun observe(): Flow<List<SearchHistoryEntity>>

    @Upsert
    suspend fun upsert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :q")
    suspend fun delete(q: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun observe(): Flow<List<SubscriptionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelUrl = :url)")
    fun isSubscribed(url: String): Flow<Boolean>

    @Upsert
    suspend fun add(entity: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelUrl = :url")
    suspend fun remove(url: String)

    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    suspend fun all(): List<SubscriptionEntity>
}
