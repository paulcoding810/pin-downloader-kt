package com.paulcoding.pindownloader.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcoding.pindownloader.ui.component.Indicator


@Composable
fun BoxScope.LoadingOverlay(maxSize: Boolean = true) {
    Box(
        modifier = if (maxSize) Modifier.fillMaxSize() else Modifier.matchParentSize(),
        contentAlignment = Alignment.Center
    ) {
        Indicator(
            modifier = Modifier.size(64.dp)
        )
    }
}