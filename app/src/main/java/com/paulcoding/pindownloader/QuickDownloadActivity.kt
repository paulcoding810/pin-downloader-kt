package com.paulcoding.pindownloader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcoding.pindownloader.component.AppExceptionText
import com.paulcoding.pindownloader.component.DownloadEffect
import com.paulcoding.pindownloader.component.LoadingOverlay
import com.paulcoding.pindownloader.ui.page.home.FetchResult
import org.koin.android.ext.android.inject

class QuickDownloadActivity : ComponentActivity() {
    private val viewModel by inject<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
            val (extractState, downloadState) = state

            DownloadView(extractState, downloadState, onAction = viewModel::dispatch)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val message = when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }

            else -> null
        }

        message?.also {
            viewModel.extractLink(it)
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadView(extractState: ExtractState, downloadState: DownloadState, onAction: (MainAction) -> Unit) {
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }

    DownloadEffect(downloadState, snackbarHostState) {
        activity?.finish()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            when (extractState) {
                is ExtractState.Error -> {
                    AppExceptionText(extractState.exception)
                }
                ExtractState.Idle -> {}
                is ExtractState.Loading -> LoadingOverlay()
                is ExtractState.Success ->
                    FetchResult(
                        pinData = extractState.pinData,
                        downloadState = downloadState,
                        showLoadingMaxSize = false,
                        onAction = onAction,
                    )
            }
        }
    }
}
