package com.paulcoding.pindownloader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        appContext = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
    }
}
