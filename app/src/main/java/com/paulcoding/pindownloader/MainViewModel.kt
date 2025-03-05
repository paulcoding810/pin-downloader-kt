package com.paulcoding.pindownloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcoding.pindownloader.App.Companion.appContext
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.extractor.pinterest.PinterestExtractor
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
import com.paulcoding.pindownloader.helper.AppPreference
import com.paulcoding.pindownloader.helper.Downloader
import com.paulcoding.pindownloader.helper.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var _uiStateFlow = MutableStateFlow(UiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val pinterestExtractor = PinterestExtractor()
    private val pixivExtractor = PixivExtractor()

    val isPremium = AppPreference.isPremium

    data class UiState(
        val input: String = "",
        val exception: AppException? = null,
        val pinData: PinData? = null,
        val isFetchingImages: Boolean = false,
        val isFetched: Boolean = false,
        val isRedirectingUrl: Boolean = false,
        val isDownloadingImage: Boolean = false,
        val isDownloadingVideo: Boolean = false,
        val isDownloaded: Boolean = false,
    )

    private suspend fun extract(link: String) {
        _uiStateFlow.update { UiState().copy(input = link) }

        val extractor =
            when {
                pinterestExtractor.isMatches(link) -> pinterestExtractor
                pixivExtractor.isMatches(link) -> pixivExtractor
                else -> null
            }

        if (extractor == null) {
            setError(AppException.InvalidUrlError(link))
            return
        }

        _uiStateFlow.update { it.copy(isFetchingImages = true) }

        try {
            extractor.extract(link).let { data ->
                _uiStateFlow.update { it.copy(pinData = data) }
            }
        } catch (e: AppException) {
            e.printStackTrace()
            setError(e)
        } catch (e: Exception) {
            e.printStackTrace()
            setError(AppException.UnknownError())
        }

        _uiStateFlow.update { it.copy(isFetchingImages = false, isFetched = true) }
    }

    private fun setError(appException: AppException) {
        _uiStateFlow.update { it.copy(exception = appException) }
    }

    fun clearPinData() {
        _uiStateFlow.update { UiState() }
    }

    fun download(
        link: String,
        type: PinType = PinType.IMAGE,
        source: PinSource = PinSource.PINTEREST,
        fileName: String? = null,
        onSuccess: (path: String) -> Unit = {}
    ) {
        if (source == PinSource.PIXIV && !isPremium.value) {
            return setError(AppException.PremiumRequired())
        }
        val headers =
            if (source == PinSource.PIXIV) mapOf("referer" to "https://www.pixiv.net/") else mapOf()

        viewModelScope.launch(Dispatchers.IO) {
            if (type == PinType.VIDEO) {
                _uiStateFlow.update { it.copy(isDownloadingVideo = true) }
            } else {
                _uiStateFlow.update { it.copy(isDownloadingImage = true) }
            }
            checkInternetOrExec {
                try {
                    val downloadPath = Downloader.download(appContext, link, fileName, headers)
                    _uiStateFlow.update { it.copy(isDownloadingVideo = false) }
                    onSuccess(downloadPath)
                } catch (e: AppException) {
                    e.printStackTrace()
                    setError(e)
                }
            }

            if (type == PinType.VIDEO) {
                _uiStateFlow.update { it.copy(isDownloadingVideo = false) }
            } else {
                _uiStateFlow.update { it.copy(isDownloadingImage = false) }
            }
        }
    }

    fun setLink(link: String) {
        _uiStateFlow.update { it.copy(input = link) }
    }

    private suspend fun checkInternetOrExec(block: suspend () -> Unit) {
        if (NetworkUtil.isInternetAvailable()) {
            return block()
        }
        return setError(AppException.NetworkError())
    }

    fun extractLink(msg: String) {
        _uiStateFlow.update { UiState().copy(input = msg) }
        viewModelScope.launch(Dispatchers.IO) {
            checkInternetOrExec {
                val urlPattern = """(https?://\S+)""".toRegex()
                val link = urlPattern.find(msg)?.value

                if (link == null) {
                    setError(AppException.MessageError())
                    return@checkInternetOrExec
                }
                setLink(link)
                extract(link)
            }
        }
    }
}