package com.paulcoding.pindownloader.helper

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.paulcoding.pindownloader.R
import java.io.File

const val PIN_DOWNLOADER_PATH_NAME = "PinDownloader"

val downloadDir =
    File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        PIN_DOWNLOADER_PATH_NAME
    )


val IMAGE_REGEX = "\\.(jpg|png|gif)$".toRegex()
val VIDEO_REGEX = "\\.(mp4|mov)$".toRegex()

fun getFiles(context: Context): Result<List<String>> = runCatching {
    if (!downloadDir.exists())
        downloadDir.mkdir()

    return@runCatching downloadDir
        .walkTopDown()
        .sortedByDescending { it.lastModified() }
        .filter { it.isFile }
        .map { file -> file.absolutePath }
        .toList()
}

fun createFileIntent(context: Context, path: String): Intent? {
    val uri = DocumentFile.fromSingleUri(context, Uri.parse(path))?.let { documentFile ->
        if (documentFile.exists()) documentFile.uri
        else if (File(path).exists())
            FileProvider.getUriForFile(context, context.getFileProvider(), File(path))
        else null
    }
    return uri?.let {
        Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            data = uri
        }
    }
}

fun viewFile(context: Context, path: String) = runCatching {
    val intent = createFileIntent(context, path)?.apply {
        action = Intent.ACTION_VIEW
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    true
}

fun shareFile(context: Context, path: String) = runCatching {
    val intent = createFileIntent(context, path)?.apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, data)
        val mime = data?.let { context.contentResolver.getType(it) } ?: "media/*"
        setDataAndType(data, mime)
        clipData = ClipData(null, ClipData.Item(data))
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_pin)))
    true
}


fun Context.getFileProvider() = "$packageName.provider"