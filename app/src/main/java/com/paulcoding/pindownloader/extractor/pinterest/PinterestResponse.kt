package com.paulcoding.pindownloader.extractor.pinterest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PinterestResponse(
    @SerialName("resource_response")
    val resourceResponse: ResourceResponse,
)

@Serializable
data class ResourceResponse(
    @SerialName("data")
    val data: Data,
)

@Serializable
data class Data(
    @SerialName("images")
    val images: Map<String, Image>?,
    @SerialName("rich_metadata")
    val richMetadata: RichMetadata?,
    @SerialName("videos")
    val videos: Videos?,
)

@Serializable
data class RichMetadata(
    @SerialName("article")
    val article: Article,
    @SerialName("title")
    val title: String?,
)

@Serializable
data class Videos(
    @SerialName("id")
    val id: String,
    @SerialName("video_list")
    val videoList: Map<String, Image>,
)

@Serializable
data class Image(
    @SerialName("height")
    val height: Int,
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int,
)

@Serializable
data class Article(
    @SerialName("date_published")
    val datePublished: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("type")
    val type: String?,
)
