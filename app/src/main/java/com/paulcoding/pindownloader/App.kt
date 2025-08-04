package com.paulcoding.pindownloader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.paulcoding.androidtools.AndroidTools
import com.paulcoding.pindownloader.di.appModule
import com.paulcoding.pindownloader.di.networkModule
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        AndroidTools.initialize(this)
        appContext = this
        connectivityManager = getSystemService()!!

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
            modules(networkModule)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context
        lateinit var connectivityManager: ConnectivityManager
    }
}
