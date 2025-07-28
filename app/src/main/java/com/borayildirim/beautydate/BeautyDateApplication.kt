package com.borayildirim.beautydate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for BeautyDate app
 * Configures Hilt for dependency injection and handles locale initialization
 */
@HiltAndroidApp
class BeautyDateApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }

} 