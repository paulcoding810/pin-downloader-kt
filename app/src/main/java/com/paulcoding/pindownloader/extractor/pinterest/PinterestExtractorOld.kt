package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class PinterestExtractorOld {
    suspend fun extract(id: String): Result<PinData> {
        return runCatching {
            val url = ""
            val response =
                KtorClient.client
                    .get(url)
                    .apply {
                        if (status != HttpStatusCode.OK) {
                            println("status==$status")
                            throw AppException.PinNotFoundError(url)
                        }
                    }.body<PinterestResponse>()

            val videos =
                response.resourceResponse.data.videos
                    ?.videoList
            val images = response.resourceResponse.data.images
            val richMetadata = response.resourceResponse.data.richMetadata

            var pinData =
                PinData(
                    thumbnail =
                    images
                        ?.values
                        ?.toList()
                        ?.get(0)
                        ?.url,
                    author = "",
                    description = richMetadata?.title ?: richMetadata?.article?.description,
                    date = "",
                    source = PinSource.PINTEREST,
                    id = "",
                    link = "",
                )

            if (videos == null && images == null) {
                throw AppException.ParseJsonError(id)
            }

            if (videos != null) {
                pinData =
                    pinData.copy(
                        video =
                        videos.values
                            .map { it.url }
                            .find { it.contains("mp4") },
                    )
            }
            if (images != null) {
                pinData =
                    pinData.copy(
                        image =
                        images["orig"]?.url
                            ?: images.values.lastOrNull()?.url,
                    )
            }

            return@runCatching pinData
        }
    }
}
