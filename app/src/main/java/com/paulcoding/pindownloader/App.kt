package com.paulcoding.pindownloader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
    }
}
