package com.paulcoding.pindownloader.ui.model

import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.PinType

data class DownloadInfo(
    val link: String,
    val type: PinType = PinType.IMAGE,
    val source: PinSource = PinSource.PINTEREST,
    val fileName: String? = null,
)