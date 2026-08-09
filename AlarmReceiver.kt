package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val scheduleId = intent.getIntExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)

        Log.d(TAG, "AlarmReceiver triggered with action: $action, scheduleId: $scheduleId")

        when (action) {
            AlarmScheduler.ACTION_SLEEP_START -> {
                val startIntent = Intent(context, SleepStartReceiver::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, scheduleId)
                }
                context.sendBroadcast(startIntent)
            }
            AlarmScheduler.ACTION_SLEEP_END -> {
                val endIntent = Intent(context, SleepEndReceiver::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, scheduleId)
                }
                context.sendBroadcast(endIntent)
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
