package com.paulcoding.pindownloader.helper

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

object KtorClient {
    val client =
        HttpClient(CIO) {
            engine {
                requestTimeout = 8000
            }
            install(ContentNegotiation) {
                json(CustomJson)
            }
            install(HttpTimeout)
        }
}

suspend fun resolveRedirectedUrl(shortUrl: String): String? {
    HttpClient(CIO) {
        followRedirects = false
    }.use { client ->
        val response: HttpResponse = client.get(shortUrl)
        return if (response.status.value in 300..308) {
            response.headers[HttpHeaders.Location]?.let { resolveRedirectedUrl(it) }
        } else {
            shortUrl
        }
    }
}
