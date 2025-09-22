package com.paulcoding.pindownloader.extractor.pinterest

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parseInputStream
import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.extractor.Extractor
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.CustomJson
import com.paulcoding.pindownloader.helper.traverseObject
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import java.net.URI

class PinterestExtractor : Extractor() {
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
        traverseObject<JsonElement>(
            response,
            listOf(
                "response",
                "data",
                "v3GetPinQuery",
                "data"
            )
        )?.let { data ->
            val id = traverseObject<String>(data, "entityId")
                ?: throw (AppException.PinNotFoundError(link))
            val imageUrl = traverseObject<String>(data, "imageSpec_orig.url")
            val thumbnail = traverseObject<String>(data, "imageSpec_236x.url")
            val title = traverseObject<String>(data, listOf("title")) ?: traverseObject<String>(
                data,
                listOf("gridTitle")
            )
            val author = traverseObject<String>(data, "closeupAttribution.fullName")
                ?: traverseObject<String>(data, "originPinner.fullName")
            val videoUrl = traverseObject<String>(
                data,
                "videos.videoList.v720P.url",
            ) ?: traverseObject<String>(
                data,
                "videos.videoList.V_HLSV3_MOBILE.url",
            ) ?: traverseObject<String>(
                data,
                "storyPinData.pages.[].blocks.[].videoDataV2.videoList720P.v720P.url",
            ) ?: traverseObject<String>(
                data,
                "storyPinData.pages.[].blocks.[].videoDataV2.videoListMobile.vHLSV3MOBILE.url"
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
            httpClient.use { client ->
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
