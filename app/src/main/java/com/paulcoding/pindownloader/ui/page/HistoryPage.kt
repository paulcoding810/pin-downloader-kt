package com.paulcoding.pindownloader.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.helper.getFiles
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.helper.viewFile

@Composable
fun HistoryPage(goBack: () -> Unit) {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<String>()) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        getFiles(context).onSuccess {
            files = it
        }.onFailure {
            it.printStackTrace()
            makeToast(it.message ?: context.getString(R.string.failed_to_get_files))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Button(onClick = goBack) {
            Text("Go Back")
        }
        files.map {
            Box(modifier = Modifier.clickable(onClick = {
                viewFile(context, it)
                    .onFailure {
                        it.printStackTrace()
                        makeToast(it.message ?: context.getString(R.string.failed_to_view_file))
                    }
            })) {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.size(width = 200.dp, height = 300.dp),
                )
//                if (it.contains(VIDEO_REGEX))
//                    VideoPlayer(
//                        videoUri = it,
//                        modifier = Modifier.size(width = 200.dp, height = 300.dp),
//                    )
            }
        }
    }
}