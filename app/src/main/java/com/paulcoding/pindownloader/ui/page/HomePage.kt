package com.paulcoding.pindownloader.ui.page

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.component.VideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val text = uiState.input

    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val view = LocalView.current

    fun submit() {
        if (text.isEmpty()) {
            makeToast("Input Empty!")
            return
        }
        keyboardController?.hide()
        viewModel.extractLink(text)
    }

    LaunchedEffect(uiState.exception) {
        uiState.exception?.let {

            when (it.message) {
                ExtractorError.PIN_NOT_FOUND -> makeToast(R.string.pin_not_found)
                ExtractorError.CANNOT_PARSE_JSON -> makeToast(R.string.failed_to_fetch_images)
                else -> makeToast(it.message ?: it.toString())
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    coroutineScope.launch {
                        delay(1000L)
                        clipboardManager.getText()?.text?.also {
                            if (it.startsWith("http") && it != text) {
                                viewModel.setLink(it)
                            }
                        }
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { viewModel.setLink(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { submit() }),
            placeholder = {
                Text(stringResource(R.string.enter_link))
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        viewModel.clearPinData()
                    }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
        )
        Button(onClick = { submit() }, enabled = text.isNotEmpty()) {
            if (uiState.isFetchingImages || uiState.isRedirectingUrl) {
                Indicator()
            } else {
                Text(LocalContext.current.getString(R.string.fetch))
            }
        }

        uiState.pinData?.let {
            FetchResult(modifier = Modifier.fillMaxSize(), viewModel)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FetchResult(
    modifier: Modifier,
    viewModel: MainViewModel
) {

    val scrollState = rememberScrollState()

    val uiState by viewModel.uiStateFlow.collectAsState()
    val pinData = uiState.pinData


    val storagePermission =
        rememberPermissionState(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
            if (!granted)
                makeToast("Permission Denied!")
        }

    fun checkPermissionOrDownload(block: () -> Unit) {
//        TODO: Check network
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || storagePermission.status == PermissionStatus.Granted) {
            block()
        } else {
            storagePermission.launchPermissionRequest()
        }
    }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        pinData?.apply {
            if (description != null) {
                Text(description)
            }

            if (video != null) {
                VideoPlayer(
                    videoUri = video,
                    modifier = Modifier.size(width = 200.dp, height = 300.dp),
                )

                Button(onClick = {
                    checkPermissionOrDownload {
                        viewModel.download(video, PinType.VIDEO, pinData.source, null)
                    }
                }) {
                    if (uiState.isDownloadingVideo) {
                        Indicator()
                    } else {
                        Text(stringResource(R.string.download_video))
                    }
                }
            }
            if (image != null) {
                val imageRequest =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(thumbnail)
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumbnail)
                        .run {
                            if (source == PinSource.PIXIV) {
                                httpHeaders(
                                    NetworkHeaders
                                        .Builder()
                                        .set("referer", "https://www.pixiv.net/")
                                        .build(),
                                )
                            }
                            build()
                        }
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier.size(width = 200.dp, height = 300.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Indicator()
                    },
                )

                Button(onClick = {
                    checkPermissionOrDownload {
                        viewModel.download(image, PinType.IMAGE, pinData.source, null) {
                        }
                    }
                }) {
                    if (uiState.isDownloadingImage) {
                        Indicator()
                    } else {
                        Text(stringResource(R.string.download_image))
                    }
                }
            }
        }
    }
}