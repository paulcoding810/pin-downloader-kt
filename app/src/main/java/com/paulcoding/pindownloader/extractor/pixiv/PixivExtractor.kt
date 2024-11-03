package com.paulcoding.pindownloader.extractor.pixiv

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.paulcoding.pindownloader.extractor.Extractor
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.CustomJson
import com.paulcoding.pindownloader.helper.KtorClient
import com.paulcoding.pindownloader.helper.alsoLog
import com.paulcoding.pindownloader.helper.traverseObject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement

class PixivExtractor : Extractor() {
    override val source: PinSource
        get() = PinSource.PIXIV
    override val idRegex: String
        get() = """https?://(?:www\.)?pixiv\.net/(?:en/)?(?:artworks|i)/(\d+)"""

    override fun extractResponse(
        response: JsonElement,
        link: String,
        id: String,
    ): PinData {
        val images = traverseObject<Map<String, String>>(response, listOf("illust", "{}", "urls"))

        return PinData(
            source = PinSource.PIXIV,
            thumbnail = images?.get("small"),
            author = traverseObject<String>(response, listOf("illust", "{}", "userName")),
            description = traverseObject<String>(response, listOf("illust", "{}", "alt")),
            date = traverseObject<String>(response, listOf("illust", "{}", "createDate")),
            image = images?.get("original"),
            video = null,
            id = id,
            link = link,
        )
    }

    override suspend fun callApi(apiUrl: String): JsonElement {
        val response =
            KtorClient.client
                .get(apiUrl)
                .apply {
                    if (status != HttpStatusCode.OK) {
                        throw (Exception(ExtractorError.PIN_NOT_FOUND))
                    }
                }.body<String>()

        val doc: Document = Ksoup.parse(html = response)
        val preloadDataEle = doc.getElementById("meta-preload-data")

        val preloadData =
            preloadDataEle
                ?.attr("content")
                ?.let {
                    CustomJson.parseToJsonElement(it).alsoLog("pixiv preloadData")
                }
        if (preloadData != null) {
            return preloadData
        }

        throw Exception(ExtractorError.CANNOT_PARSE_JSON, Exception(apiUrl))
    }

    override fun buildApi(
        link: String,
        id: String,
    ): String = link
}
