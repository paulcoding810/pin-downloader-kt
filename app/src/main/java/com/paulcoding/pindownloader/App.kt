package com.paulcoding.pindownloader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.paulcoding.androidtools.AndroidTools
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        AndroidTools.initialize(this)
        appContext = this
        connectivityManager = getSystemService()!!
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
        lateinit var connectivityManager: ConnectivityManager
    }
}
