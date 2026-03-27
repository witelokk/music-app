package com.witelokk.musicapp

import android.app.Application
import org.koin.android.ext.koin.androidContext

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@Application)
        }
    }
}