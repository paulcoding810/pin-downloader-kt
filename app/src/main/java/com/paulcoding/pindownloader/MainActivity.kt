package com.paulcoding.pindownloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.paulcoding.pindownloader.ui.page.AppEntry
import com.paulcoding.pindownloader.ui.theme.PinDownloaderTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get()

        enableEdgeToEdge()
        setContent {
            PinDownloaderTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppEntry(viewModel = viewModel)
                }
            }
        }
        handleIntent(intent)
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
