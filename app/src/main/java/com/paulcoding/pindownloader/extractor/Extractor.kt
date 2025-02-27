package com.paulcoding.pindownloader.extractor

import com.paulcoding.pindownloader.AppException
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
                            throw (AppException.PinNotFoundError(apiUrl))
                        }
                    }.body<JsonElement>()
            }

        return response
    }


    abstract suspend fun extract(link: String): PinData
}