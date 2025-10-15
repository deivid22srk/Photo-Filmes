package com.telegram.videoorganizer

import android.app.Application
import com.telegram.videoorganizer.data.preferences.PreferencesManager
import com.telegram.videoorganizer.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TelegramApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        val preferencesManager = PreferencesManager(this)
        
        applicationScope.launch {
            val loggingEnabled = preferencesManager.loggingEnabled.first()
            Logger.init(this@TelegramApp, loggingEnabled)
            Logger.i("TelegramApp", "Application started")
        }
    }
}
