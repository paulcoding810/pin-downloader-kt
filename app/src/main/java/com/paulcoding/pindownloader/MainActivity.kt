package com.paulcoding.pindownloader

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.get
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.component.VideoPlayer
import com.paulcoding.pindownloader.ui.theme.PinDownloaderTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel = ViewModelProvider(this).get()

        enableEdgeToEdge()
        setContent {
            PinDownloaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PinForm(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun PinForm(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
) {
    val viewState by viewModel.viewStateFlow.collectAsState()
    val pinData by viewModel.pinDataStateFlow.collectAsState()

    val isLoading = viewState == MainViewModel.State.FetchingImages

    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("https://www.pinterest.com/pin/951315121267065596/") }

    val view = LocalView.current

    fun submit() {
        println("link=$text")
        if (text.isEmpty()) {
            makeToast("Input Empty!")
            return
        }
        keyboardController?.hide()
        viewModel.extract(text)
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    coroutineScope.launch {
                        delay(1000L)
                        clipboardManager.getText()?.text?.also {
                            if (it.startsWith("http")) {
                                if (it != text) {
                                    text = it
                                }
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
            value = text,
            onValueChange = { text = it },
            keyboardOptions =
            KeyboardOptions(
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
                        text = ""
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
            if (isLoading) {
                Indicator()
            } else {
                Text(LocalContext.current.getString(R.string.fetch))
            }
        }

        pinData?.apply {
            if (description != null) {
                Text(description)
            }

            if (video != null) {
                VideoPlayer(
                    videoUri = video,
                    modifier = Modifier.size(width = 200.dp, height = 300.dp),
                )

                Button(onClick = { viewModel.download(video) }) {
                    Text(stringResource(R.string.download_video))
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

                Button(onClick = { viewModel.download(image) }) {
                    Text(stringResource(R.string.download_image))
                }
            }
        }
    }
}
