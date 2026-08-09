package com.example.util

import com.example.data.SleepSchedule
import com.example.data.SleepScheduleDao
import com.example.data.UserPreferences
import org.json.JSONArray
import org.json.JSONObject

object ExportImportHelper {

    suspend fun exportDataToJson(dao: SleepScheduleDao, prefs: UserPreferences): String {
        val root = JSONObject()

        // Export Preferences
        val prefsObj = JSONObject().apply {
            put("strictMode", prefs.getStrictModeEnabled())
            put("foregroundProtection", prefs.getForegroundProtectionEnabled())
            put("rebootRecovery", prefs.getRebootRecoveryEnabled())
            put("recoveryWorker", prefs.getRecoveryWorkerEnabled())
            put("accessibilityProtection", prefs.getAccessibilityProtectionEnabled())
        }
        root.put("preferences", prefsObj)

        // Export Schedules
        val schedules = dao.getAllSchedulesSync()
        val schedulesArr = JSONArray()
        for (s in schedules) {
            val item = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                put("enabled", s.enabled)
                put("repeatDays", JSONArray(s.repeatDays))
                put("strictModeEnabled", s.strictModeEnabled)
                put("createdAt", s.createdAt)
            }
            schedulesArr.put(item)
        }
        root.put("schedules", schedulesArr)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String, dao: SleepScheduleDao, prefs: UserPreferences): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            if (root.has("preferences")) {
                val p = root.getJSONObject("preferences")
                if (p.has("strictMode")) prefs.setStrictModeEnabled(p.getBoolean("strictMode"))
                if (p.has("foregroundProtection")) prefs.setForegroundProtectionEnabled(p.getBoolean("foregroundProtection"))
                if (p.has("rebootRecovery")) prefs.setRebootRecoveryEnabled(p.getBoolean("rebootRecovery"))
                if (p.has("recoveryWorker")) prefs.setRecoveryWorkerEnabled(p.getBoolean("recoveryWorker"))
                if (p.has("accessibilityProtection")) prefs.setAccessibilityProtectionEnabled(p.getBoolean("accessibilityProtection"))
            }

            if (root.has("schedules")) {
                val arr = root.getJSONArray("schedules")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val daysArr = obj.getJSONArray("repeatDays")
                    val daysList = mutableListOf<Int>()
                    for (d in 0 until daysArr.length()) {
                        daysList.add(daysArr.getInt(d))
                    }

                    val schedule = SleepSchedule(
                        id = if (obj.has("id")) obj.getInt("id") else 0,
                        title = obj.optString("title", "Imported Schedule"),
                        startTime = obj.optString("startTime", "23:00"),
                        endTime = obj.optString("endTime", "06:00"),
                        enabled = obj.optBoolean("enabled", true),
                        repeatDays = daysList,
                        strictModeEnabled = obj.optBoolean("strictModeEnabled", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                    dao.insertSchedule(schedule)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
