package com.bvue.debug

import android.util.Log
import com.bvue.data.repository.YoutubeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TEMPORARY (Phase 1 verification): runs one search + stream resolution + trending fetch and logs
 * the results to logcat so we can confirm the extraction layer works end-to-end. Removed in Phase 2.
 *
 *   adb logcat -s BVueProbe:I
 */
object DebugProbe {
    private const val TAG = "BVueProbe"

    fun run(repo: YoutubeRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "===== SEARCH: 'lofi hip hop' =====")
                val results = repo.search("lofi hip hop")
                Log.i(TAG, "search returned ${results.size} items")
                results.take(5).forEach {
                    Log.i(TAG, "• ${it.title} | ${it.uploader} | ${it.durationSeconds}s | ${it.url}")
                }
                results.firstOrNull()?.let { first ->
                    Log.i(TAG, "===== RESOLVE STREAMS: ${first.title} =====")
                    val data = repo.resolveStreams(first.url)
                    Log.i(
                        TAG,
                        "muxed=${data.muxed.size}, videoOnly=${data.videoOnly.size}, audioOnly=${data.audioOnly.size}",
                    )
                    data.muxed.forEach { Log.i(TAG, "  MUXED ${it.qualityLabel}: ${it.url.take(90)}") }
                    data.videoOnly.forEach { Log.i(TAG, "  VIDEO ${it.qualityLabel}: ${it.url.take(90)}") }
                    data.audioOnly.forEach { Log.i(TAG, "  AUDIO ${it.qualityLabel}: ${it.url.take(90)}") }
                }
                Log.i(TAG, "===== TRENDING =====")
                val trending = runCatching { repo.getTrending() }.getOrElse {
                    Log.w(TAG, "trending failed: ${it.message}")
                    emptyList()
                }
                Log.i(TAG, "trending returned ${trending.size} items")
                trending.take(3).forEach { Log.i(TAG, "↗ ${it.title} | ${it.uploader}") }
            } catch (t: Throwable) {
                Log.e(TAG, "Extraction probe FAILED", t)
            }
        }
    }
}
