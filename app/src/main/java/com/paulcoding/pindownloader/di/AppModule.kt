package com.paulcoding.pindownloader.di

import com.paulcoding.pindownloader.BuildConfig
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.helper.CustomJson
import com.paulcoding.pindownloader.helper.Downloader
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
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
        HttpClient(Android) {
            engine {
                connectTimeout = 10_000
                socketTimeout = 10_000
            }
            install(ContentNegotiation) {
                json(CustomJson)
            }
            install(HttpTimeout)
            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
                logger = Logger.SIMPLE
            }
        }
    }
}