package com.paulcoding.pindownloader.ui.page.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
import com.paulcoding.pindownloader.DownloadState
import com.paulcoding.pindownloader.MainAction
import com.paulcoding.pindownloader.component.DownloadIcon
import com.paulcoding.pindownloader.component.LoadingOverlay
import com.paulcoding.pindownloader.extractor.PinData
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.component.VideoPlayer
import com.paulcoding.pindownloader.ui.model.DownloadInfo
import com.paulcoding.pindownloader.ui.theme.PinDownloaderTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FetchResult(
    modifier: Modifier = Modifier,
    pinData: PinData,
    downloadState: DownloadState,
    showLoadingMaxSize: Boolean = true,
    onAction: (MainAction) -> Unit,
) {
    val storagePermission =
        rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
            if (!granted) makeToast("Permission Denied!")
        }

    fun checkPermissionOrDownload(block: () -> Unit) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || storagePermission.status == PermissionStatus.Granted) {
            block()
        } else {
            storagePermission.launchPermissionRequest()
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pinData.apply {
                if (description != null) {
                    Text(description)
                }

                if (video != null) {
                    DownloadContent(
                        onClick = {
                            checkPermissionOrDownload {
                                onAction(
                                    MainAction.Download(
                                        downloadInfo = DownloadInfo(
                                            link = video,
                                            type = PinType.VIDEO,
                                            source = pinData.source,
                                        )
                                    )
                                )
                            }
                        }) {
                        VideoPlayer(
                            videoUri = video,
                            modifier = Modifier.size(width = 350.dp, height = 500.dp),
                        )
                    }
                }
                if (image != null) {
                    val imageRequest =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(image)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(id)
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

                    DownloadContent(onClick = {
                        checkPermissionOrDownload {
                            onAction(
                                MainAction.Download(
                                    downloadInfo = DownloadInfo(
                                        link = image,
                                        type = PinType.IMAGE,
                                        source = pinData.source,
                                    )
                                )
                            )
                        }
                    }) {
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Indicator()
                            },
                        )
                    }
                }
            }
        }
        if (downloadState is DownloadState.Loading) {
            LoadingOverlay(showLoadingMaxSize)
        }
    }
}

@Composable
private fun DownloadContent(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box {
        content()
        FilledIconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onClick
        ) {
            Icon(DownloadIcon, "Download")
        }
    }
}


@Preview
@Composable
private fun Preview() {
    PinDownloaderTheme() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            FetchResult(
                pinData = PinData(
                    id = "123",
                    link = "",
                    source = PinSource.PINTEREST,
                    description = "",
                    image = "",
                    video = null
                ),
                downloadState = DownloadState.Idle,
                onAction = {}
            )
        }
    }
}