package com.paulcoding.pindownloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcoding.pindownloader.App.Companion.appContext
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.extractor.pinterest.PinterestExtractor
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
import com.paulcoding.pindownloader.helper.AppPreference
import com.paulcoding.pindownloader.helper.Downloader
import com.paulcoding.pindownloader.helper.alsoLog
import com.paulcoding.pindownloader.helper.resolveRedirectedUrl
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
        val exception: Throwable? = null,
        val pinData: PinData? = null,
        val isFetchingImages: Boolean = false,
        val isRedirectingUrl: Boolean = false,
        val isDownloadingImage: Boolean = false,
        val isDownloadingVideo: Boolean = false
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
            setError(Exception(ExtractorError.INVALID_URL))
            return
        }

        _uiStateFlow.update { it.copy(isFetchingImages = true) }

        extractor
            .extract(link)
            .alsoLog("pin extraction")
            .onSuccess { data ->
                _uiStateFlow.update { it.copy(pinData = data) }

            }.onFailure { throwable ->
                setError(throwable)
            }

        _uiStateFlow.update { it.copy(isFetchingImages = false) }
    }

    private fun setError(throwable: Throwable) {
        _uiStateFlow.update { it.copy(exception = throwable) }
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
            return setError(Exception(ExtractorError.PREMIUM_REQUIRED))
        }
        val headers =
            if (source == PinSource.PIXIV) mapOf("referer" to "https://www.pixiv.net/") else mapOf()

        viewModelScope.launch(Dispatchers.IO) {
            if (type == PinType.VIDEO) {
                _uiStateFlow.update { it.copy(isDownloadingVideo = true) }
            } else {
                _uiStateFlow.update { it.copy(isDownloadingImage = true) }
            }
            Downloader.download(appContext, link, fileName, headers)
                .alsoLog("download path")
                .onSuccess(onSuccess)
                .onFailure { setError(it) }
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

    fun extractLink(msg: String) {
        _uiStateFlow.update { UiState().copy(input = msg) }
        viewModelScope.launch(Dispatchers.IO) {
            var link: String?

            val urlPattern = """(https?://\S+)""".toRegex()

            link = urlPattern.find(msg)?.value

            if (link == null) {
                setError(Exception(ExtractorError.INVALID_URL))
                return@launch
            }

            val isRedirected = """https?://pin.it/\S+""".toRegex().matches(link)

            if (isRedirected) {
                try {
                    _uiStateFlow.update { it.copy(isRedirectingUrl = true) }
                    link = resolveRedirectedUrl(link).alsoLog("redirectedUrl")
                } catch (e: Exception) {
                    setError(e)
                } finally {
                    _uiStateFlow.update { it.copy(isRedirectingUrl = false) }
                }
            }

            if (link != null) {
                setLink(link)
                extract(link)
            } else {
                setError(Exception(ExtractorError.INVALID_URL))
            }
        }
    }
}
