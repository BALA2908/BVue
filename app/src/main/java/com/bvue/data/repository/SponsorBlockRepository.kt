package com.bvue.data.repository

import com.bvue.domain.model.SponsorSegment
import com.bvue.util.UserAgents
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder

/**
 * SponsorBlock (https://sponsor.ajay.app) — crowd-sourced skip segments for a video.
 * Fail-open by design: any network/parse error or "no segments" (HTTP 404) returns an empty list,
 * so playback is never blocked or broken by this feature.
 */
class SponsorBlockRepository(
    private val client: OkHttpClient,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun fetchSegments(videoId: String, categories: Collection<String>): List<SponsorSegment> =
        withContext(io) {
            if (videoId.isBlank() || categories.isEmpty()) return@withContext emptyList()
            runCatching {
                val cats = JSONArray(categories.toList()).toString()
                val url = "https://sponsor.ajay.app/api/skipSegments?videoID=" +
                    URLEncoder.encode(videoId, "UTF-8") +
                    "&categories=" + URLEncoder.encode(cats, "UTF-8")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", UserAgents.DESKTOP_CHROME)
                    .build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@use emptyList()
                    val body = resp.body?.string().orEmpty()
                    if (body.isBlank()) return@use emptyList()
                    val arr = JSONArray(body)
                    buildList {
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val seg = obj.getJSONArray("segment")
                            val startMs = (seg.getDouble(0) * 1000).toLong()
                            val endMs = (seg.getDouble(1) * 1000).toLong()
                            val category = obj.optString("category", "sponsor")
                            if (endMs > startMs) add(SponsorSegment(category, startMs, endMs))
                        }
                    }.sortedBy { it.startMs }
                }
            }.getOrDefault(emptyList())
        }
}
