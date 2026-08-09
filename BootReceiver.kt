package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON") {

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = UserPreferences(context)
                    if (prefs.getRebootRecoveryEnabled()) {
                        val db = SleepDatabase.getDatabase(context)
                        val enabledSchedules = db.sleepScheduleDao().getEnabledSchedulesSync()

                        Log.d(TAG, "Restoring ${enabledSchedules.size} enabled schedules on boot")
                        for (schedule in enabledSchedules) {
                            AlarmScheduler.scheduleAlarmsForSchedule(context, schedule)
                        }

                        if (prefs.getForegroundProtectionEnabled()) {
                            SleepProtectionService.startService(context)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring schedules on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
