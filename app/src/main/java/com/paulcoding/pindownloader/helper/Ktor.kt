package com.paulcoding.pindownloader.helper

import io.ktor.client.*
import io.ktor.client.engine.cio.* // or OkHttp for Android-specific configurations
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

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
