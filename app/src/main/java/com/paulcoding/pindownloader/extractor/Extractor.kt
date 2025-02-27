package com.paulcoding.pindownloader.extractor

import com.paulcoding.pindownloader.helper.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

abstract class Extractor {
    abstract val source: PinSource
    abstract val idRegex: String


    open fun isMatches(link: String): Boolean {
        val regex = Regex(idRegex)
        return regex.containsMatchIn(link)
    }

    open fun buildApi(
        link: String,
        id: String,
    ): String = link

    protected open suspend fun callApi(apiUrl: String): JsonElement {
        val response =
            KtorClient.client.use { client ->
                client.get(apiUrl)
                    .apply {
                        if (status != HttpStatusCode.OK) {
                            throw (Exception(ExtractorError.PIN_NOT_FOUND))
                        }
                    }.body<JsonElement>()
            }

        return response
    }


    abstract suspend fun extract(link: String): Result<PinData>
}

object ExtractorError {
    const val NO_INTERNET = "No Internet."
    const val FAILED_TO_DOWNLOAD = "Failed to download image."
    const val INVALID_URL = "Invalid URL."
    const val PIN_NOT_FOUND = "Pin not found."
    const val CANNOT_PARSE_ID = "Cannot parse Id."
    const val CANNOT_PARSE_JSON = "Cannot parse JSON."
    const val PREMIUM_REQUIRED = "Premium Required."
}

@Serializable
data class PinData(
    val id: String,
    val link: String,
    val source: PinSource,
    val thumbnail: String?,
    val author: String?,
    val description: String?,
    val date: String?,
    val image: String? = null,
    val video: String? = null,
) {
    val isPixiv = source == PinSource.PIXIV
    val isPinterest = source == PinSource.PINTEREST
    val isVideo = video != null
    val isImage = image != null && video == null
}

enum class PinSource {
    PINTEREST,
    PIXIV,
}

enum class PinType {
    IMAGE,
    VIDEO,
}