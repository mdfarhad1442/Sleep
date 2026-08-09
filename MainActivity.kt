package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.SleepHourTheme
import com.example.worker.RecoveryWorker

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = UserPreferences(applicationContext)

        // Initialize WorkManager Periodic Recovery Worker if enabled
        if (prefs.getRecoveryWorkerEnabled()) {
            RecoveryWorker.schedulePeriodicRecovery(applicationContext)
        }

        // Start Foreground Protection Service if enabled
        if (prefs.getForegroundProtectionEnabled()) {
            SleepProtectionService.startService(applicationContext)
        }

        setContent {
            SleepHourTheme(darkTheme = true) {
                MainAppNavigation()
            }
        }
    }
}
