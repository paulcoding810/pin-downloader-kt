package com.paulcoding.pindownloader.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.paulcoding.pindownloader.R
import com.paulcoding.pindownloader.helper.Config
import com.paulcoding.pindownloader.helper.VIDEO_REGEX
import com.paulcoding.pindownloader.helper.getFiles
import com.paulcoding.pindownloader.helper.makeToast
import com.paulcoding.pindownloader.helper.viewFile
import com.paulcoding.pindownloader.ui.component.ColumnWithAd
import com.paulcoding.pindownloader.ui.icon.PlayCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(goBack: () -> Unit) {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<String>()) }

    val configuration = LocalConfiguration.current
    val itemWidth = (configuration.screenWidthDp.dp - 24.dp) / 2

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
                            Badge {
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
        ColumnWithAd(modifier = Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files.size) { index ->
                    val file = files[index]
                    Box(modifier = Modifier.clickable(onClick = {
                        viewFile(context, file)
                            .onFailure {
                                it.printStackTrace()
                                makeToast(
                                    it.message ?: context.getString(R.string.failed_to_view_file)
                                )
                            }
                    })) {
                        Box {
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(width = itemWidth, height = itemWidth),
                            )
                            if (file.contains(VIDEO_REGEX))
                                Icon(
                                    imageVector = PlayCircle,
                                    "Play",
                                    modifier = Modifier
                                        .align(
                                            Alignment.Center
                                        )
                                        .size(64.dp),
                                    tint = Color.White
                                )
                        }
                    }
                }
            }
        }
    }
}