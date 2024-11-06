package com.paulcoding.pindownloader.ui.page.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.component.VideoPlayer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FetchResult(
    modifier: Modifier,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val pinData = uiState.pinData
    val isPremium by viewModel.isPremium.collectAsState()

    val storagePermission =
        rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
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

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
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
                        viewModel.download(image, PinType.IMAGE, pinData.source, null)
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
}