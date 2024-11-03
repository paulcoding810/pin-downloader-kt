package com.paulcoding.pindownloader.helper

import android.content.Context
import android.os.Environment
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import java.io.File
import java.io.FileOutputStream

const val folderName = "PinDownloader"

object Downloader {
    private fun getFileNameFromUrl(url: String): String {

        val trimmedUrl = url.removeSuffix("/")
        log(url, "url")
        log(trimmedUrl, "trimmed")
        return trimmedUrl.substringAfterLast("/")
    }

    private fun getDownloadDir(context: Context): File {
        val downloadDir =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)
            } else {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    folderName
                )
            }

        // Create the directory if it doesn't exist
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        return downloadDir
    }

    suspend fun download(
        context: Context,
        imageUrl: String,
        fileName: String? = null
    ): String? {
        val name = fileName ?: getFileNameFromUrl(imageUrl)
        val file = File(getDownloadDir(context), name)

        try {
            KtorClient.client.get(imageUrl).readRawBytes().let { bytes ->
                FileOutputStream(file).use { fos ->
                    fos.write(bytes)
                }
            }

            if (file.exists()) {
                println("Image saved successfully at ${file.absolutePath}")
                return file.absolutePath
            } else {
                println("Failed to save image.")
            }
        } catch (e: Exception) {
            println("Failed to download image: ${e.message}")
        } finally {
        }

        return null
    }
}
