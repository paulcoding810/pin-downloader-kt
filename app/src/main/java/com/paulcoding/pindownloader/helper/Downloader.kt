package com.paulcoding.pindownloader.helper

import android.content.Context
import com.paulcoding.pindownloader.extractor.ExtractorError
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.readRawBytes
import java.io.File
import java.io.FileOutputStream


object Downloader {
    private fun getFileNameFromUrl(url: String): String {
        val trimmedUrl = url.removeSuffix("/")
        return trimmedUrl.substringAfterLast("/")
    }

    private fun getDownloadDir(context: Context): File {
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        return downloadDir
    }

    suspend fun download(
        context: Context,
        imageUrl: String,
        fileName: String? = null,
        customHeaders: Map<String, String> = mapOf(),
    ): Result<String> {
        return runCatching {
            val name = fileName ?: getFileNameFromUrl(imageUrl)
            val file = File(getDownloadDir(context), name)

            KtorClient.client.use { client ->
                client.get(imageUrl) {
                    headers {
                        customHeaders.forEach { (key, value) ->
                            append(key, value)
                        }
                    }
                }
                    .readRawBytes().let { bytes ->
                        FileOutputStream(file).use { fos ->
                            fos.write(bytes)
                        }
                    }
            }

            if (file.exists()) {
                return@runCatching file.absolutePath
            }
            throw Exception(ExtractorError.FAILED_TO_DOWNLOAD)
        }
    }
}
