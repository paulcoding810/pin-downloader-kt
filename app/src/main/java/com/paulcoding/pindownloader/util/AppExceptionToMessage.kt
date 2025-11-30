package com.paulcoding.pindownloader.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.R

@Composable
fun AppException.toMessage(): String = when (this) {
    is AppException.PinNotFoundError -> stringResource(R.string.pin_not_found)
    is AppException.InvalidUrlError -> stringResource(R.string.invalid_url)
    is AppException.NetworkError -> stringResource(R.string.network_error)
    is AppException.DownloadError -> stringResource(R.string.failed_to_download)
    is AppException.ParseJsonError -> stringResource(R.string.failed_to_fetch_images)
    is AppException.ParseIdError -> stringResource(R.string.failed_to_fetch_images)
    is AppException.UnknownError -> stringResource(R.string.something_went_wrong)
    else -> this.message ?: this.toString()
}

fun AppException.toMessage(context: Context): String = when (this) {
    is AppException.PinNotFoundError -> context.getString(R.string.pin_not_found)
    is AppException.InvalidUrlError -> context.getString(R.string.invalid_url)
    is AppException.NetworkError -> context.getString(R.string.network_error)
    is AppException.DownloadError -> context.getString(R.string.failed_to_download)
    is AppException.ParseJsonError -> context.getString(R.string.failed_to_fetch_images)
    is AppException.ParseIdError -> context.getString(R.string.failed_to_fetch_images)
    is AppException.UnknownError -> context.getString(R.string.something_went_wrong)
    else -> this.message ?: this.toString()
}
