package com.paulcoding.pindownloader.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.helper.Config
import com.paulcoding.pindownloader.helper.getFiles
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.helper.viewFile

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Gallery") },
                modifier = Modifier.padding(8.dp),
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                }, actions = {
                    if (files.isNotEmpty() && Config.showDeleteButton)
                        BadgedBox(badge = {
                            Badge() {
                                Text(files.size.toString())
                            }
                        }) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    PlainTooltip { Text(stringResource(R.string.delete_images)) }
                                },
                                state = rememberTooltipState(),
                            ) {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Filled.Delete, "Delete Images", tint = Color.Red)
                                }
                            }
                        }
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
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

}