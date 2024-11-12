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
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
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
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.extractor.ExtractorError
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.ui.component.Indicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    viewHistory: () -> Unit,
    navToPremium: () -> Unit,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val text = uiState.input

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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
        focusManager.clearFocus()
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
                        Icon(Icons.AutoMirrored.Outlined.List, "History")
                    }
//                    IconButton(onClick = navToPremium) {
//                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Premium")
//                    }
                },
            )
        }
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
            }

            uiState.pinData?.let {
                FetchResult(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp), viewModel
                )
            }
        }
    }
}
