package com.paulcoding.pindownloader.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paulcoding.pindownloader.BuildConfig
import com.paulcoding.pindownloader.R

@Composable
fun ColumnWithAd(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
        if (BuildConfig.DEBUG) BannerAd(stringResource(R.string.ad_unit_id))
    }
}