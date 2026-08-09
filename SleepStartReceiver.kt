package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.ui.screens.SleepActivity
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
        Log.d(TAG, "SleepStartReceiver triggered for scheduleId: $scheduleId")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SleepDatabase.getDatabase(context)
                val prefs = UserPreferences(context)

                // Start Foreground Service if enabled
                if (prefs.getForegroundProtectionEnabled()) {
                    SleepProtectionService.startService(context)
                }

                if (scheduleId != -1) {
                    val schedule = db.sleepScheduleDao().getScheduleById(scheduleId)
                    if (schedule != null) {
                        // Re-schedule alarms for future repeat days
                        AlarmScheduler.scheduleAlarmsForSchedule(context, schedule)
                    }
                }

                // Open Sleep Screen
                val sleepIntent = Intent(context, SleepActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                }
                context.startActivity(sleepIntent)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing sleep start", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SleepStartReceiver"
    }
}
