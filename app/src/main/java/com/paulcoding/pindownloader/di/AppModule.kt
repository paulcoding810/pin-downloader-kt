package com.paulcoding.pindownloader.di

import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.helper.CustomJson
import com.paulcoding.pindownloader.helper.Downloader
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
    singleOf(::Downloader)
    single { CustomJson }
}

val networkModule = module {
    single<HttpClient> {
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
}