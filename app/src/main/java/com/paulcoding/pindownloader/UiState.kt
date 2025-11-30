package com.paulcoding.pindownloader

import coil3.Bitmap
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.ui.model.DownloadInfo

data class UiState(
    val extractState: ExtractState = ExtractState.Idle,
    val downloadState: DownloadState = DownloadState.Idle,
)

sealed class ExtractState {
    data object Idle : ExtractState()
    class Loading(val url: String) : ExtractState()
    class Success(val pinData: PinData, bitmap: Bitmap?) : ExtractState()
    class Error(val exception: AppException) : ExtractState()
}

sealed class DownloadState {
    data object Idle : DownloadState()
    class Loading(val url: String) : DownloadState()
    class Success(val downloadedPath: String) : DownloadState()
    class Error(val exception: AppException) : DownloadState()
}

sealed class MainAction {
    class ExtractLink(val url: String) : MainAction()
    class Download(val downloadInfo: DownloadInfo) : MainAction()

    data object ClearPinData : MainAction()
}