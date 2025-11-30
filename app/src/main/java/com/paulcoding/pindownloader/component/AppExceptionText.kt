package com.paulcoding.pindownloader.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.util.toMessage

@Composable
fun AppExceptionText(exception: AppException) {
    Text(
        text = exception.toMessage(),
        color = MaterialTheme.colorScheme.error
    )
}