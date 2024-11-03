package com.paulcoding.pindownloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcoding.pindownloader.App.Companion.appContext
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.pinterest.PinterestExtractor
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
import com.paulcoding.pindownloader.helper.Downloader
import com.paulcoding.pindownloader.helper.alsoLog
import com.paulcoding.pindownloader.helper.log
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.helper.resolveRedirectedUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private var _viewStateFlow = MutableStateFlow<State>(State.Idle)
    val viewStateFlow = _viewStateFlow.asStateFlow()

    private var _pinDataStateFlow = MutableStateFlow<PinData?>(null)
    val pinDataStateFlow = _pinDataStateFlow.asStateFlow()

    private var _urlStateFlow = MutableStateFlow("")
    val urlStateFlow = _urlStateFlow.asStateFlow()

    private val pinterestExtractor = PinterestExtractor()
    private val pixivExtractor = PixivExtractor()

    sealed class State {
        data object Idle : State()

        data object FetchingImages : State()
        data object Downloading : State()
    }

    private suspend fun extract(link: String) {
        _pinDataStateFlow.update { null }

        val extractor =
            when {
                pinterestExtractor.isMatches(link) -> pinterestExtractor
                pixivExtractor.isMatches(link) -> pixivExtractor
                else -> null
            } ?: return onFetchError(Exception(ExtractorError.INVALID_URL))

        _viewStateFlow.update { State.FetchingImages }

        extractor
            .extract(link)
            .alsoLog("pin extraction")
            .onSuccess { data ->
                _pinDataStateFlow.update { data }
            }.onFailure { throwable ->
                onFetchError(throwable)
            }

        _viewStateFlow.update { State.Idle }
    }

    private suspend fun onFetchError(e: Throwable) {
        withContext(Dispatchers.Main) {

            when (e.message) {
                ExtractorError.PIN_NOT_FOUND -> makeToast(R.string.pin_not_found)
                ExtractorError.CANNOT_PARSE_JSON -> makeToast(R.string.failed_to_fetch_images)
                else -> makeToast(e.message ?: e.toString())
            }

            e.printStackTrace()
        }
    }

    fun clearPinData() {
        _urlStateFlow.update { "" }
        _pinDataStateFlow.update { null }
    }

    fun download(
        link: String,
        source: PinSource = PinSource.PINTEREST,
        fileName: String? = null,
        onSuccess: (path: String?) -> Unit = {}
    ) {
        val headers =
            if (source == PinSource.PIXIV) mapOf("referer" to "https://www.pixiv.net/") else mapOf()

        viewModelScope.launch(Dispatchers.IO) {
            _viewStateFlow.update { State.Downloading }

            val path = Downloader.download(appContext, link, fileName, headers)
                .alsoLog("download path")
            onSuccess(path)
            _viewStateFlow.update { State.Idle }
        }
    }

    fun setLink(link: String) {
        _urlStateFlow.update { link }
    }

    fun extractLink(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var link: String?

            val urlPattern = """(https?://\S+)""".toRegex()

            link = urlPattern.find(msg)?.value

            if (link == null)
                return@launch

            val isRedirected = """https?://pin.it/\S+""".toRegex().matches(link)

            if (isRedirected) {
                try {
                    _viewStateFlow.update { State.FetchingImages }
                    link = resolveRedirectedUrl(link).alsoLog("redirectedUrl")
                } catch (e: Exception) {
                    onFetchError(e)
                } finally {
                    _viewStateFlow.update { State.Idle }
                }
            }

            if (link != null) {
                _urlStateFlow.update { link }
                extract(link)
            } else {
                log("Invalid URL")
            }
        }
    }
}
