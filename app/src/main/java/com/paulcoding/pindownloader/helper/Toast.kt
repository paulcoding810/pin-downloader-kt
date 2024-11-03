package com.paulcoding.pindownloader.helper

import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.paulcoding.pindownloader.App.Companion.appContext

@MainThread
fun makeToast(
    @StringRes stringId: Int,
) {
    appContext.apply {
        Toast.makeText(this, getString(stringId), Toast.LENGTH_SHORT).show()
    }
}

@MainThread
fun makeToast(message: String?) {
    appContext.apply {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
