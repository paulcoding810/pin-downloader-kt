package com.paulcoding.pindownloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.google.android.gms.ads.MobileAds
import com.paulcoding.pindownloader.ui.PremiumViewModel
import com.paulcoding.pindownloader.ui.page.AppEntry
import com.paulcoding.pindownloader.ui.theme.PinDownloaderTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var premiumViewModel: PremiumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get()
        premiumViewModel = ViewModelProvider(this).get()

        enableEdgeToEdge()
        setContent {
            PinDownloaderTheme {
                AppEntry(viewModel = viewModel, premiumViewModel = premiumViewModel)
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
