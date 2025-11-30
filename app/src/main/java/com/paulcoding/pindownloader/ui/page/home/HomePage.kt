package com.paulcoding.pindownloader.ui.page.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.paulcoding.androidtools.makeToast
import com.paulcoding.pindownloader.ExtractState
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.component.AppExceptionText
import com.paulcoding.pindownloader.component.DownloadEffect
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.icon.History
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    viewHistory: () -> Unit,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val (extractState, downloadState) = uiState
    var text by remember { mutableStateOf("https://www.pinterest.com/pin/70298444178786767") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboard.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val snackBarHostState = remember { SnackbarHostState() }

    fun submit() {
        if (text.isEmpty()) {
            makeToast("Input Empty!")
            return
        }
        keyboardController?.hide()
        focusManager.clearFocus()
        viewModel.extractLink(text)
    }

    DownloadEffect(downloadState, snackBarHostState)

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    coroutineScope.launch {
                        delay(1000L)
                        val clipData = clipboardManager.getClipEntry()?.clipData ?: return@launch
                        if (clipData.itemCount == 0) return@launch

                        val link = clipData.getItemAt(0).text?.toString() ?: return@launch
                        if (link.startsWith("http") && link != text) {
                            text = link
                        }
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name), style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp
                        )
                    )
                },
                modifier = Modifier.padding(8.dp),
                actions = {
                    IconButton(onClick = viewHistory) {
                        Icon(History, "History")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            Column(
                modifier = modifier
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = { text = it },
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

                when (extractState) {
                    ExtractState.Idle -> {
                        Button(onClick = { submit() }, enabled = text.isNotEmpty()) {
                            Text(LocalContext.current.getString(R.string.fetch))
                        }
                    }
                    is ExtractState.Loading -> Indicator()
                    is ExtractState.Success -> FetchResult(
                        pinData = extractState.pinData,
                        downloadState = downloadState,
                        onAction = viewModel::dispatch,
                    )
                    is ExtractState.Error -> {
                        AppExceptionText(extractState.exception)
                    }
                }
            }
        }
    }
}

