package com.paulcoding.pindownloader.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    val exoPlayer =
        remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUri.toUri()))
                prepare()
                playWhenReady = true

                addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            isLoading = playbackState == Player.STATE_BUFFERING
                        }
                    },
                )
            }
        }

    Box(modifier = modifier) {
        AndroidView(factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        }, modifier = Modifier.fillMaxSize())

        if (isLoading) {
            Indicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    DisposableEffect(
        Unit,
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
