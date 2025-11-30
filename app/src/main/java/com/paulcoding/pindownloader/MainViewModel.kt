package com.paulcoding.pindownloader

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Bitmap
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.paulcoding.pindownloader.App.Companion.appContext
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.pinterest.PinterestExtractor
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
import com.paulcoding.pindownloader.helper.Downloader
import com.paulcoding.pindownloader.helper.NetworkUtil
import com.paulcoding.pindownloader.ui.model.DownloadInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val downloader: Downloader) : ViewModel() {
    private var _state = MutableStateFlow<UiState>(UiState())
    val uiStateFlow = _state.asStateFlow()

    private val pinterestExtractor = PinterestExtractor()
    private val pixivExtractor = PixivExtractor()


    private suspend fun extract(link: String) {
        _state.update { it.copy(extractState = ExtractState.Loading(link)) }

        val extractor =
            when {
                pinterestExtractor.isMatches(link) -> pinterestExtractor
                pixivExtractor.isMatches(link) -> pixivExtractor
                else -> null
            }

        if (extractor == null) {
            setExtractError(AppException.InvalidUrlError(link))
            return
        }

        try {
            extractor.extract(link).let { data ->
                val bitmap = data.image?.let {
                    downloadImageBitmap(appContext, it)
                }
                _state.update { it.copy(extractState = ExtractState.Success(data, bitmap)) }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is AppException) {
                setExtractError(e)
            }
            setExtractError(AppException.UnknownError())
        }
    }

    private fun setExtractError(appException: AppException) {
        _state.update { it.copy(extractState = ExtractState.Error(appException)) }
    }

    fun clearPinData() {
        _state.value = UiState()
    }

    private suspend fun downloadImageBitmap(context: Context, url: String): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Disable hardware bitmaps if needed
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            return (result.image as BitmapImage).bitmap
        }
        return null
    }

    fun dispatch(action: MainAction) {
        when (action) {
            is MainAction.ExtractLink -> extractLink(action.url)
            is MainAction.Download -> download(action.downloadInfo)
            is MainAction.ClearPinData -> clearPinData()
        }
    }

    private fun download(downloadInfo: DownloadInfo) {
        val (link, type, source, fileName) = downloadInfo

        val headers =
            if (source == PinSource.PIXIV) mapOf("referer" to "https://www.pixiv.net/") else mapOf()

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(downloadState = DownloadState.Loading(link)) }
            checkInternetOrExec {
                try {
                    val downloadPath = downloader.download(appContext, link, fileName, headers)
                    _state.update { it.copy(downloadState = DownloadState.Success(downloadPath)) }
                } catch (e: Exception) {
                    e.printStackTrace()

                    if (e is AppException)
                        _state.update { it.copy(downloadState = DownloadState.Error(e)) }
                    else
                        _state.update { it.copy(downloadState = DownloadState.Error(AppException.DownloadError(link))) }
                }
            }
        }
    }

    private suspend fun checkInternetOrExec(block: suspend () -> Unit) {
        if (NetworkUtil.isInternetAvailable()) {
            return block()
        }
        return setExtractError(AppException.NetworkError())
    }

    fun extractLink(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            checkInternetOrExec {
                val urlPattern = """(https?://\S+)""".toRegex()
                val link = urlPattern.find(msg)?.value

                if (link == null) {
                    setExtractError(AppException.MessageError())
                    return@checkInternetOrExec
                }
                extract(link)
            }
        }
    }
}