package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
        Log.d(TAG, "SleepEndReceiver triggered for scheduleId: $scheduleId")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SleepDatabase.getDatabase(context)
                val prefs = UserPreferences(context)

                if (scheduleId != -1) {
                    val schedule = db.sleepScheduleDao().getScheduleById(scheduleId)
                    if (schedule != null) {
                        prefs.incrementSleepSessionsCompleted()
                        prefs.addSleepMinutesLogged(schedule.getDurationMinutes())

                        // Re-schedule alarms for future repeat days
                        AlarmScheduler.scheduleAlarmsForSchedule(context, schedule)
                    }
                }

                // Send broadcast to finish SleepActivity if open
                val finishIntent = Intent(ACTION_FINISH_SLEEP_ACTIVITY)
                context.sendBroadcast(finishIntent)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing sleep end", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SleepEndReceiver"
        const val ACTION_FINISH_SLEEP_ACTIVITY = "com.example.sleephour.ACTION_FINISH_SLEEP_ACTIVITY"
    }
}
