package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.SleepSchedule
import com.example.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    const val ACTION_SLEEP_START = "com.example.sleephour.ACTION_SLEEP_START"
    const val ACTION_SLEEP_END = "com.example.sleephour.ACTION_SLEEP_END"
    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"

    fun scheduleAlarmsForSchedule(context: Context, schedule: SleepSchedule) {
        if (!schedule.enabled) {
            cancelAlarmsForSchedule(context, schedule)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Calculate next start trigger
        val nextStartMillis = calculateNextTriggerTime(schedule.startTime, schedule.repeatDays, isEnd = false)
        if (nextStartMillis > System.currentTimeMillis()) {
            val startIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SLEEP_START
                putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            }
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.id * 10 + 1,
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setExactAndAllowWhileIdle(alarmManager, nextStartMillis, startPendingIntent)
            Log.d(TAG, "Scheduled START alarm for schedule ${schedule.id} at $nextStartMillis")
        }

        // Calculate next end trigger
        val nextEndMillis = calculateNextTriggerTime(schedule.endTime, schedule.repeatDays, isEnd = true, startTime = schedule.startTime)
        if (nextEndMillis > System.currentTimeMillis()) {
            val endIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SLEEP_END
                putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            }
            val endPendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.id * 10 + 2,
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setExactAndAllowWhileIdle(alarmManager, nextEndMillis, endPendingIntent)
            Log.d(TAG, "Scheduled END alarm for schedule ${schedule.id} at $nextEndMillis")
        }
    }

    fun cancelAlarmsForSchedule(context: Context, schedule: SleepSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SLEEP_START
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id * 10 + 1,
            startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
        }

        val endIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SLEEP_END
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id * 10 + 2,
            endIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (endPendingIntent != null) {
            alarmManager.cancel(endPendingIntent)
            endPendingIntent.cancel()
        }
    }

    private fun setExactAndAllowWhileIdle(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun calculateNextTriggerTime(
        timeStr: String,
        repeatDays: List<Int>,
        isEnd: Boolean,
        startTime: String? = null
    ): Long {
        val parts = timeStr.split(":").map { it.toIntOrNull() ?: 0 }
        val hour = parts[0]
        val minute = parts[1]

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (repeatDays.isEmpty()) {
            return calendar.timeInMillis
        }

        // Loop up to 7 days to find the next matching repeat day
        for (i in 0..7) {
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            if (repeatDays.contains(dayOfWeek)) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
