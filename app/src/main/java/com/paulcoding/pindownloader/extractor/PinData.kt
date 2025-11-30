package com.paulcoding.pindownloader.extractor

import kotlinx.serialization.Serializable

@Serializable
data class PinData(
    val id: String,
    val link: String,
    val source: PinSource,
    val description: String? = null,
    val image: String? = null,
    val video: String? = null,
)

enum class PinSource {
    PINTEREST,
    PIXIV,
}

enum class PinType {
    IMAGE,
    VIDEO,
}