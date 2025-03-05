package com.paulcoding.pindownloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulcoding.pindownloader.ui.component.Indicator
import com.paulcoding.pindownloader.ui.page.home.FetchResult

class QuickDownloadActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            DownloadView(viewModel)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadView(viewModel: MainViewModel) {
    val sheetState = rememberModalBottomSheetState()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val width = LocalConfiguration.current.screenWidthDp.dp
    val context = LocalActivity.current

    println("state.isDownloaded ${state.isFetched}")
    LaunchedEffect(state.isFetched) {
        if (state.isFetched) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { context?.finish() },
        sheetState = sheetState,
    ) {
        if (state.isFetchingImages) Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width - 32.dp)
        ) {
            Indicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
        }

        FetchResult(
            modifier = Modifier.fillMaxWidth(),
            viewModel = viewModel,
            onDownloaded = { context?.finish() }
        )
    }
}