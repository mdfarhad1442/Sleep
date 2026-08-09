package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.ui.screens.SleepActivity
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SleepProtectionService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            verifyAndEnforceProtection()
        }

        return START_STICKY
    }

    private suspend fun verifyAndEnforceProtection() {
        val db = SleepDatabase.getDatabase(applicationContext)
        val prefs = UserPreferences(applicationContext)

        while (serviceScope.isActive) {
            try {
                if (prefs.getForegroundProtectionEnabled()) {
                    val enabledSchedules = db.sleepScheduleDao().getEnabledSchedulesSync()

                    // Check if any schedule is currently active right now
                    var isAnyScheduleActive = false
                    for (schedule in enabledSchedules) {
                        // Re-verify alarms for each schedule
                        AlarmScheduler.scheduleAlarmsForSchedule(applicationContext, schedule)

                        if (schedule.isCurrentlyActive()) {
                            isAnyScheduleActive = true
                        }
                    }

                    // If a schedule is active and strict mode is on, trigger SleepActivity if not already foreground
                    if (isAnyScheduleActive && prefs.getStrictModeEnabled()) {
                        val sleepIntent = Intent(applicationContext, SleepActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        applicationContext.startActivity(sleepIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in protection verification loop", e)
            }

            // Loop every 30 seconds
            delay(30_000)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(getString(R.string.notification_content))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private const val TAG = "SleepProtectionService"
        const val CHANNEL_ID = "sleep_hour_protection_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, SleepProtectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SleepProtectionService::class.java)
            context.stopService(intent)
        }
    }
}
