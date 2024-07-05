package com.julianczaja.stations

import android.app.Application
import timber.log.Timber

class StationsApp: Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
