package com.paulcoding.pindownloader.extractor

import com.paulcoding.pindownloader.AppException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import org.koin.java.KoinJavaComponent.inject

abstract class Extractor {
    protected val httpClient: HttpClient by inject(HttpClient::class.java)
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
            httpClient.get(apiUrl)
                .apply {
                    if (status != HttpStatusCode.OK) {
                        throw (AppException.PinNotFoundError(apiUrl))
                    }
                }.body<JsonElement>()

        return response
    }


    abstract suspend fun extract(link: String): PinData
}