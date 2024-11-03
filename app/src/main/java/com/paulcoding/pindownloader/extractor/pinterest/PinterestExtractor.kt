package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.extractor.Extractor
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinImage
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.traverseObject
import kotlinx.serialization.json.JsonElement

class PinterestExtractor : Extractor() {
    override val source: PinSource
        get() = PinSource.PINTEREST

    override val idRegex: String
        get() = """https?://(?:www\.)?pinterest\.[a-z]+/(?:pin|board)/(\d+)/?"""

    override fun buildApi(
        link: String,
        id: String,
    ): String =
        """https://www.pinterest.com/resource/PinResource/get/?data={"options":{"id":"$id","field_set_key":"unauth_react_main_pin"}}"""

    override fun extractResponse(
        response: JsonElement,
        link: String,
        id: String,
    ): PinData {
        traverseObject<JsonElement>(response, listOf("resource_response", "data"))?.let { data ->
            val images = traverseObject<Map<String, PinImage>>(data, listOf("images"))
            val videos =
                traverseObject<Map<String, PinImage>>(
                    data,
                    listOf("videos", "video_list"),
                )

            val storyVideos =
                traverseObject<Map<String, PinImage>>(
                    data,
                    listOf(
                        "story_pin_data",
                        "pages",
                        "[]",
                        "blocks",
                        "[]",
                        "video",
                        "video_list",
                    ),
                )

            if (videos == null && images == null) {
                throw Exception(ExtractorError.CANNOT_PARSE_JSON)
            }

            var pinData =
                PinData(
                    thumbnail = images?.get("564x")?.url,
                    author = "",
                    description = traverseObject<String>(data, listOf("seo_description")),
                    date = traverseObject<String>(data, listOf("closeup_attribution", "full_name")),
                    source = PinSource.PINTEREST,
                    id = id,
                    link = link,
                )

            pinData = pinData.copy(
                video = getVideoUrl(videos) ?: getVideoUrl(storyVideos),
            )

            if (images != null) {
                val origin = images["orig"]?.url ?: images.values.lastOrNull()?.url
                val thumb =
                    if (origin != null && origin.endsWith("gif")) origin else pinData.thumbnail
                pinData =
                    pinData.copy(
                        image = origin,
                        thumbnail = thumb
                    )
            }
            return pinData
        }
        throw Exception(ExtractorError.CANNOT_PARSE_JSON)
    }

    private fun getVideoUrl(videos: Map<String, PinImage>?): String? {
        if (videos == null) return null
        val url = videos["V_720P"]?.url
            ?: videos["V_EXP7"]?.url
            ?: videos.values
                .map { it.url }
                .find { it.contains("mp4") }
        return url
    }
}
