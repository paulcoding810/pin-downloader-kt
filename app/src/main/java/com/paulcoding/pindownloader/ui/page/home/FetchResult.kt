package com.paulcoding.pindownloader.ui.page.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.paulcoding.androidtools.makeToast
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.helper.viewFile
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.component.VideoPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FetchResult(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onDownloaded: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiStateFlow.collectAsState()
    val pinData = uiState.pinData
    val isPremium by viewModel.isPremium.collectAsState()

    val storagePermission =
        rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
            if (!granted)
                makeToast("Permission Denied!")
        }


    fun onSuccess(path: String) {
        coroutineScope.launch {

            val result = snackbarHostState.showSnackbar(
                context.getString(R.string.downloaded_successfully),
                context.getString(R.string.view),
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewFile(context, path)
                    onDownloaded?.invoke()
                }

                SnackbarResult.Dismissed -> {}
            }
        }
    }

    fun checkPermissionOrDownload(block: () -> Unit) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || storagePermission.status == PermissionStatus.Granted) {
            block()
        } else {
            storagePermission.launchPermissionRequest()
        }
    }

    Box {
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pinData?.apply {
                if (description != null) {
                    Text(description)
                }

                if (video != null) {
                    VideoPlayer(
                        videoUri = video,
                        modifier = Modifier.size(width = 350.dp, height = 500.dp),
                    )

                    Button(onClick = {
                        checkPermissionOrDownload {
                            viewModel.download(
                                video,
                                PinType.VIDEO,
                                pinData.source,
                                null,
                            ) {
                                onSuccess(it)
                            }
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
                        modifier = Modifier.size(width = 350.dp, height = 500.dp),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Indicator()
                        },
                    )

                    Button(onClick = {
                        checkPermissionOrDownload {
                            viewModel.download(image, PinType.IMAGE, pinData.source, null) {
                                onSuccess(it)
                            }
                        }
                    }) {
                        if (uiState.isDownloadingImage) {
                            Indicator()
                        } else {
                            Text(stringResource(R.string.download_image))
                            if (isPixiv && !isPremium) Text(" 👑")
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}