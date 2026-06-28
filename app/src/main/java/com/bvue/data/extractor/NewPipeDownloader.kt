package com.bvue.data.extractor

import com.bvue.util.UserAgents
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException

/**
 * NewPipeExtractor ships no HTTP client (master-brief rule 1): it exposes an abstract [Downloader]
 * with a single [execute] method that we implement over OkHttp — modeled on NewPipe's reference
 * DownloaderImpl, using a realistic desktop browser User-Agent.
 */
class NewPipeDownloader(private val client: OkHttpClient) : Downloader() {

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend: ByteArray? = request.dataToSend()

        val requestBody = dataToSend?.toRequestBody(null, 0, dataToSend.size)

        val builder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody)
            .url(url)

        for ((headerName, headerValues) in headers) {
            builder.removeHeader(headerName)
            for (value in headerValues) {
                builder.addHeader(headerName, value)
            }
        }
        if (headers.keys.none { it.equals("User-Agent", ignoreCase = true) }) {
            builder.header("User-Agent", UserAgents.DESKTOP_CHROME)
        }

        client.newCall(builder.build()).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("reCaptcha Challenge requested", url)
            }
            val body = response.body?.string()
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                body,
                response.request.url.toString(),
            )
        }
    }
}
