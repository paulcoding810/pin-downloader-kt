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
import com.paulcoding.pindownloader.helper.makeToast
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

    private val pinterestExtractor = PinterestExtractor()
    private val pixivExtractor = PixivExtractor()

    sealed class State {
        data object Idle : State()

        data object FetchingImages : State()
        data object Downloading : State()
    }

    fun extract(link: String) {
        val extractor =
            when {
                pinterestExtractor.isMatches(link) -> pinterestExtractor
                pixivExtractor.isMatches(link) -> pixivExtractor
                else -> null
            } ?: return onFetchError(Exception(ExtractorError.INVALID_URL))

        viewModelScope.launch(Dispatchers.IO) {
            _viewStateFlow.update { State.FetchingImages }
            extractor
                .extract(link)
                .alsoLog("pin extraction")
                .onSuccess { data ->
                    _pinDataStateFlow.update { data }
                }.onFailure { throwable ->
                    withContext(Dispatchers.Main) {
                        onFetchError(throwable)
                    }
                }

            _viewStateFlow.update { State.Idle }
        }
    }

    private fun onFetchError(e: Throwable) {
        when (e.message) {
            ExtractorError.PIN_NOT_FOUND -> makeToast(R.string.pin_not_found)
            ExtractorError.CANNOT_PARSE_JSON -> makeToast(R.string.failed_to_fetch_images)
            else -> makeToast(e.message ?: e.toString())
        }

        e.printStackTrace()
    }

    fun clearPinData() {
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
}
