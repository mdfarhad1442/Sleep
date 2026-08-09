package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.util.AlarmScheduler
import java.util.concurrent.TimeUnit

class RecoveryWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "RecoveryWorker verifying database, alarms, and services...")

        try {
            val prefs = UserPreferences(context)
            if (!prefs.getRecoveryWorkerEnabled()) {
                return Result.success()
            }

            val db = SleepDatabase.getDatabase(context)
            val enabledSchedules = db.sleepScheduleDao().getEnabledSchedulesSync()

            // Verify and re-register alarms
            for (schedule in enabledSchedules) {
                AlarmScheduler.scheduleAlarmsForSchedule(context, schedule)
            }

            // Verify foreground service
            if (prefs.getForegroundProtectionEnabled()) {
                SleepProtectionService.startService(context)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in RecoveryWorker", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "RecoveryWorker"
        const val WORK_NAME = "sleep_hour_recovery_work"

        fun schedulePeriodicRecovery(context: Context) {
            val constraints = Constraints.Builder().build()
            val recoveryRequest = PeriodicWorkRequestBuilder<RecoveryWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                recoveryRequest
            )
        }

        fun cancelPeriodicRecovery(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
