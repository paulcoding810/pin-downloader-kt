package com.paulcoding.pindownloader.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.paulcoding.androidtools.AndroidTools.context
import com.paulcoding.androidtools.makeToast
import com.paulcoding.pindownloader.DownloadState
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.helper.viewFile
import com.paulcoding.pindownloader.util.toMessage

@Composable
fun DownloadEffect(downloadState: DownloadState, snackBarHostState: SnackbarHostState, onSuccess : () -> Unit = {}) {
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is DownloadState.Error -> {
                makeToast(downloadState.exception.toMessage(context))
            }
            is DownloadState.Success -> {
                val result = snackBarHostState.showSnackbar(
                    context.getString(R.string.downloaded_successfully),
                    context.getString(R.string.view),
                    duration = SnackbarDuration.Short,
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        viewFile(context, downloadState.downloadedPath)
                        onSuccess()
                    }
                    SnackbarResult.Dismissed -> {
                    }
                }
            }
            else -> {}
        }
    }
}