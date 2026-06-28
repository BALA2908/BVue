package com.bvue.data.repository

import com.bvue.domain.model.ChannelData
import com.bvue.domain.model.StreamData
import com.bvue.domain.model.VideoItem

/**
 * All NewPipeExtractor calls are blocking (rule 2), so every function here is a suspend function
 * that runs on Dispatchers.IO inside the implementation. UI/ViewModels must never call the
 * extractor directly.
 */
interface YoutubeRepository {
    suspend fun search(query: String): List<VideoItem>
    suspend fun getTrending(): List<VideoItem>
    suspend fun resolveStreams(videoUrl: String): StreamData
    suspend fun getChannel(channelUrl: String): ChannelData
    suspend fun getShortsFeed(query: String = "tamil shorts"): List<VideoItem>
}
