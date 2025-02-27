package com.paulcoding.pindownloader.extractor

import kotlinx.serialization.Serializable

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