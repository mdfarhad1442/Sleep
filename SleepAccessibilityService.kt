package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.ui.screens.SleepActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SleepAccessibilityService : AccessibilityService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val packageName = event.packageName?.toString() ?: return

            // Don't intercept if current package is our own package
            if (packageName == applicationContext.packageName) return

            serviceScope.launch {
                checkAndEnforceSleepMode(packageName)
            }
        }
    }

    private suspend fun checkAndEnforceSleepMode(foregroundPackage: String) {
        val prefs = UserPreferences(applicationContext)

        if (!prefs.getAccessibilityProtectionEnabled() || !prefs.getStrictModeEnabled()) {
            return
        }

        val db = SleepDatabase.getDatabase(applicationContext)
        val enabledSchedules = db.sleepScheduleDao().getEnabledSchedulesSync()

        val isSleepTimeActive = enabledSchedules.any { it.isCurrentlyActive() }

        if (isSleepTimeActive) {
            Log.d(TAG, "Sleep time active! User attempted to switch to package: $foregroundPackage. Re-opening Sleep Screen.")

            val intent = Intent(applicationContext, SleepActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
            applicationContext.startActivity(intent)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val TAG = "SleepAccessibility"
    }
}
