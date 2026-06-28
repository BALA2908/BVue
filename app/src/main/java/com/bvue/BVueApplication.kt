package com.bvue

import android.app.Application
import com.bvue.di.AppContainer
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class BVueApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Rule 1: initialize NewPipeExtractor exactly once. Bias results toward Tamil / India so
        // suggestions, trending, and Shorts surface Tamil content (the user's preference).
        NewPipe.init(
            container.downloader,
            Localization("ta", "IN"),
            ContentCountry("IN"),
        )
    }
}
