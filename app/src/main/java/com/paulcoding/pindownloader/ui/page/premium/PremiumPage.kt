package com.paulcoding.pindownloader.ui.page.premium

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paulcoding.pindownloader.ui.PremiumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPage(viewModel: PremiumViewModel, goBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = goBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                }
            })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Premium Features")
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("- Download From Pixiv\n")
                    Text("- Interval wallpaper\n")
                    Text("- Hide from Gallery\n")
                }
                Spacer(modifier = Modifier.padding(top = 36.dp))
                Button(
                    onClick = {
                        viewModel.startBillingClientConnection()
                    }) {
                    Text("Enroll!")
                }
            }
        }
    }
}