package com.bvue.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.bvue.data.extractor.NewPipeDownloader
import com.bvue.data.local.BVueDatabase
import com.bvue.data.local.MIGRATION_2_3
import com.bvue.data.repository.LibraryRepository
import com.bvue.data.repository.SponsorBlockRepository
import com.bvue.data.repository.YoutubeRepository
import com.bvue.data.repository.YoutubeRepositoryImpl
import com.bvue.data.settings.SettingsRepository
import com.bvue.player.PlayerMediaSourceFactory
import com.bvue.player.SleepTimerController
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.downloader.Downloader
import java.util.concurrent.TimeUnit

/**
 * Manual DI graph (Hilt is overkill for a solo single-module app). Singletons created lazily and
 * held by [com.bvue.BVueApplication].
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    /** True after the cold-start brand intro has played; process-scoped so it shows once per launch. */
    var introShown: Boolean = false

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val downloader: Downloader by lazy { NewPipeDownloader(okHttpClient) }

    val youtubeRepository: YoutubeRepository by lazy { YoutubeRepositoryImpl() }

    val sponsorBlockRepository: SponsorBlockRepository by lazy { SponsorBlockRepository(okHttpClient) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    private val database: BVueDatabase by lazy {
        Room.databaseBuilder(appContext, BVueDatabase::class.java, "bvue.db")
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val libraryRepository: LibraryRepository by lazy { LibraryRepository(database) }

    val mediaSourceFactory: PlayerMediaSourceFactory by lazy { PlayerMediaSourceFactory(okHttpClient) }

    /**
     * Single app-scoped ExoPlayer shared by the watch UI and the background PlaybackService
     * (same process). Configured per rules 6 & 7: handles audio focus and survives screen-off.
     */
    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(appContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /** App-scoped sleep timer so a countdown survives leaving the watch screen / backgrounding. */
    val sleepTimerController: SleepTimerController by lazy { SleepTimerController(exoPlayer) }
}
