package com.paulcoding.pindownloader.extractor.pixiv

import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.extractor.Extractor
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.traverseObject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class PixivExtractor : Extractor() {
    override val source: PinSource
        get() = PinSource.PIXIV
    override val idRegex: String
        get() = """https?://(?:www\.)?pixiv\.net/(?:en/)?(?:artworks|i)/(\d+)"""

    private fun extractId(link: String): String {
        val regex = Regex(idRegex)
        val matchResult = regex.find(link)
        if (matchResult != null) {
            return matchResult.groupValues[1]
        }

        throw AppException.ParseIdError(link)
    }

    private fun extractResponse(
        response: JsonElement,
        link: String,
    ): PinData {
        val id = extractId(link)
        val images = traverseObject<Map<String, String>>(response, listOf("body", "urls"))
            ?: throw AppException.ParseJsonError(link)

        return PinData(
            source = PinSource.PIXIV,
            description = traverseObject<String>(response, listOf("body", "title")),
            image = images["original"],
            video = null,
            id = id,
            link = link,
        )
    }

    override suspend fun callApi(apiUrl: String): JsonElement {
        try {
            val response =
                httpClient.get(apiUrl) {
                    header("Accept", "application/json")
                }
                    .apply {
                        if (status != HttpStatusCode.OK) {
                            throw AppException.PinNotFoundError(apiUrl)
                        }
                    }.body<JsonObject>()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            throw AppException.ParseJsonError(apiUrl)
        }
    }

    override suspend fun extract(link: String): PinData {
        val id = extractId(link)
        val response = callApi(buildApi(link, id))
        return extractResponse(response, link)
    }

    override fun buildApi(
        link: String,
        id: String,
    ): String = "https://www.pixiv.net/ajax/illust/$id"
}
