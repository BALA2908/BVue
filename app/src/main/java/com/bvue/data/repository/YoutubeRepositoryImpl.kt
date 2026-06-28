package com.bvue.data.repository

import com.bvue.domain.model.ChannelData
import com.bvue.domain.model.ContentRestriction
import com.bvue.domain.model.RestrictedContentException
import com.bvue.domain.model.StreamData
import com.bvue.domain.model.StreamKind
import com.bvue.domain.model.StreamOption
import com.bvue.domain.model.VideoItem
import com.bvue.util.formatRelativeDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.extractor.exceptions.PrivateContentException
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.VideoStream

class YoutubeRepositoryImpl(
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : YoutubeRepository {

    private val service: StreamingService get() = ServiceList.YouTube

    override suspend fun search(query: String): List<VideoItem> = withContext(io) {
        val handler = service.searchQHFactory.fromQuery(query)
        val extractor = service.getSearchExtractor(handler)
        extractor.fetchPage()
        extractor.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .map { it.toVideoItem() }
    }

    override suspend fun getTrending(): List<VideoItem> = withContext(io) {
        val kiosk = service.kioskList.defaultKioskExtractor
        kiosk.fetchPage()
        kiosk.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .map { it.toVideoItem() }
    }

    override suspend fun resolveStreams(videoUrl: String): StreamData = withContext(io) {
        try {
            val info = StreamInfo.getInfo(service, videoUrl)
            StreamData(
                videoId = info.id,
                title = info.name,
                uploader = info.uploaderName ?: "",
                uploaderUrl = info.uploaderUrl,
                viewCount = info.viewCount,
                uploadDate = formatRelativeDate(info.uploadDate?.offsetDateTime()) ?: info.textualUploadDate,
                durationSeconds = info.duration,
                relatedItems = info.relatedItems.filterIsInstance<StreamInfoItem>().map { it.toVideoItem() },
                muxed = info.videoStreams.map { it.toOption(StreamKind.MUXED) },
                videoOnly = info.videoOnlyStreams.map { it.toOption(StreamKind.VIDEO_ONLY) },
                audioOnly = info.audioStreams.map { it.toAudioOption() },
            )
        } catch (e: AgeRestrictedContentException) {
            throw RestrictedContentException(ContentRestriction.AGE_RESTRICTED, e.message)
        } catch (e: PaidContentException) {
            throw RestrictedContentException(ContentRestriction.PAID, e.message)
        } catch (e: GeographicRestrictionException) {
            throw RestrictedContentException(ContentRestriction.GEO, e.message)
        } catch (e: PrivateContentException) {
            throw RestrictedContentException(ContentRestriction.UNAVAILABLE, e.message)
        } catch (e: ContentNotAvailableException) {
            throw RestrictedContentException(ContentRestriction.UNAVAILABLE, e.message)
        }
    }

    override suspend fun getChannel(channelUrl: String): ChannelData = withContext(io) {
        val info = ChannelInfo.getInfo(service, channelUrl)
        val videosTab = info.tabs.firstOrNull { it.contentFilters.contains(ChannelTabs.VIDEOS) }
            ?: info.tabs.firstOrNull()
        val videos = videosTab?.let { tab ->
            runCatching {
                ChannelTabInfo.getInfo(service, tab).relatedItems
                    .filterIsInstance<StreamInfoItem>()
                    .map { it.toVideoItem() }
            }.getOrDefault(emptyList())
        } ?: emptyList()
        ChannelData(
            name = info.name,
            avatarUrl = info.avatars.lastOrNull()?.url,
            subscriberCount = info.subscriberCount,
            videos = videos,
        )
    }

    override suspend fun getShortsFeed(query: String): List<VideoItem> = withContext(io) {
        // No Google login → no personalized Shorts feed; assemble Tamil short-form videos.
        val handler = service.searchQHFactory.fromQuery(query)
        val extractor = service.getSearchExtractor(handler)
        extractor.fetchPage()
        val items = extractor.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .map { it.toVideoItem() }
        // Shorts often report 0/unknown duration in search results; keep those + clips ≤90s,
        // and never return an empty feed.
        val shorts = items.filter { it.durationSeconds in 0..90 }
        if (shorts.isNotEmpty()) shorts else items
    }

    private fun StreamInfoItem.toVideoItem(): VideoItem {
        val vid = runCatching { service.streamLHFactory.getId(url) }.getOrDefault(url)
        val uploaded = formatRelativeDate(uploadDate?.offsetDateTime()) ?: textualUploadDate
        return VideoItem(
            id = vid,
            url = url,
            title = name,
            uploader = uploaderName ?: "",
            uploaderUrl = uploaderUrl,
            durationSeconds = duration,
            viewCount = viewCount,
            uploadDate = uploaded,
            thumbnailUrl = thumbnails.lastOrNull()?.url,
        )
    }

    private fun VideoStream.toOption(kind: StreamKind): StreamOption {
        val h = resolution?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
        return StreamOption(
            url = content,
            kind = kind,
            qualityLabel = resolution ?: "${h}p",
            isVideo = true,
            height = h,
        )
    }

    private fun AudioStream.toAudioOption(): StreamOption = StreamOption(
        url = content,
        kind = StreamKind.AUDIO_ONLY,
        qualityLabel = "${averageBitrate}kbps",
        isVideo = false,
        bitrate = averageBitrate,
        language = audioLocale?.language,
        trackName = audioTrackName,
    )
}
