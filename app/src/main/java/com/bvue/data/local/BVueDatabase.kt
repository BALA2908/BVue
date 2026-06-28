package com.bvue.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        ResumePositionEntity::class,
        PlaylistEntity::class,
        PlaylistVideoEntity::class,
        SearchHistoryEntity::class,
        SubscriptionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class BVueDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun resumePositionDao(): ResumePositionDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun subscriptionDao(): SubscriptionDao
}

/**
 * v2 -> v3 only ADDS two tables (search_history, subscriptions). A real migration preserves the
 * user's existing watch history / favorites / playlists / resume positions (no destructive wipe).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `search_history` " +
                "(`query` TEXT NOT NULL, `searchedAt` INTEGER NOT NULL, PRIMARY KEY(`query`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `subscriptions` " +
                "(`channelUrl` TEXT NOT NULL, `name` TEXT NOT NULL, `avatarUrl` TEXT, " +
                "`subscribedAt` INTEGER NOT NULL, PRIMARY KEY(`channelUrl`))",
        )
    }
}
