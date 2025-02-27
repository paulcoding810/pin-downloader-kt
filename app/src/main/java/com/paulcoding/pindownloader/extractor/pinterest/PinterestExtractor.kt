package com.paulcoding.pindownloader.extractor.pinterest

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseInputStream
import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.extractor.Extractor
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.CustomJson
import com.paulcoding.pindownloader.helper.KtorClient
import com.paulcoding.pindownloader.helper.traverseObject
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import java.net.URI

class PinterestExtractor() : Extractor() {
    override val source: PinSource
        get() = PinSource.PINTEREST

    override val idRegex: String
        get() = """https?://(?:www\.)?pinterest\.[a-z]+/(?:pin|board)/(\d+)/?"""

    override fun isMatches(link: String): Boolean {
        val pinterestRegex =
            Regex("""https?://(www\.)?(pinterest\.(com|co\.[a-z]{2})|pin\.it)/[a-zA-Z0-9_/-]+""")
        return pinterestRegex.containsMatchIn(link)
    }

    private fun extractResponse(
        response: JsonElement,
        link: String,
    ): PinData {
        // response.data.v3GetPinQuery.data.videos.videoList.v720P.url
        // response.data.v3GetPinQuery.data.storyPinData.pages[0].blocks[0].videoData.videoList720P.v720P.url
        // response.data.v3GetPinQuery.data.videos.videoList.v720P.thumbnail

        // response.data.v3GetPinQuery.data.imageSpec_236x.url
        // response.data.v3GetPinQuery.data.imageSpec_orig.url

        // response.data.v3GetPinQuery.data.closeupAttribution.fullName
        // response.data.v3GetPinQuery.data.title
        // response.data.v3GetPinQuery.data.gridTitle

        // response.data.v3GetPinQuery.data.entityId

        traverseObject<JsonElement>(
            response,
            listOf(
                "response",
                "data",
                "v3GetPinQuery",
                "data"
            )
        )?.let { data ->
            val id = traverseObject<String>(data, "entityId".split('.'))
                ?: throw (AppException.PinNotFoundError(link))
            val imageUrl = traverseObject<String>(data, "imageSpec_orig.url".split('.'))
            val thumbnail = traverseObject<String>(data, "imageSpec_236x.url".split('.'))
            val title = traverseObject<String>(data, listOf("title")) ?: traverseObject<String>(
                data,
                listOf("gridTitle")
            )
            val author = traverseObject<String>(data, "closeupAttribution.fullName".split('.'))
                ?: traverseObject<String>(data, "originPinner.fullName".split('.'))
            val videoUrl = traverseObject<String>(
                data,
                "videos.videoList.v720P.url".split('.'),
            ) ?: traverseObject<String>(
                data,
                "storyPinData.pages.[].blocks.[].videoData.videoList720P.v720P.url".split('.'),
            )

            if (videoUrl == null && imageUrl == null) {
                throw AppException.PinNotFoundError(link)
            }

            val pinData =
                PinData(
                    thumbnail = thumbnail ?: imageUrl,
                    author = author,
                    description = title,
                    date = null,
                    source = PinSource.PINTEREST,
                    id = id,
                    link = link,
                    video = videoUrl,
                    image = imageUrl
                )
            return pinData
        }

        throw AppException.ParseJsonError(link)
    }

    override suspend fun callApi(apiUrl: String): JsonElement {

        val uri = URI(apiUrl)
        val baseUri = "${uri.scheme}://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"

        val response =
            KtorClient.client.use { client ->
                client.get(apiUrl)
                    .apply {
                        if (status != HttpStatusCode.OK) {
                            throw AppException.PinNotFoundError(apiUrl)
                        }
                    }
            }

        val doc =
            Ksoup.parseInputStream(
                response.readRawBytes().inputStream(),
                baseUri = baseUri,
                charsetName = "UTF-8",
            )


        val initialData =
            doc.select("script[data-relay-response=true][type=application/json]").last()
                ?.html() // use html() for get script's inner text

        if (initialData.isNullOrEmpty()) {
            throw AppException.ParseJsonError(apiUrl)
        }
        return CustomJson.parseToJsonElement(initialData)
    }

    override suspend fun extract(link: String): PinData {
        val response = callApi(link)
        return extractResponse(response, link)
    }
}
