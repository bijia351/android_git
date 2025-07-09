package com.anguomob.git

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GitRadarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
