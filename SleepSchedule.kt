package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Entity(tableName = "sleep_schedules")
data class SleepSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val startTime: String, // HH:mm format, e.g. "23:00"
    val endTime: String,   // HH:mm format, e.g. "06:00"
    val enabled: Boolean = true,
    val repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 2=Tue... 7=Sun
    val strictModeEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isCurrentlyActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!enabled) return false

        val currentDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val startParts = startTime.split(":").map { it.toIntOrNull() ?: 0 }
        val endParts = endTime.split(":").map { it.toIntOrNull() ?: 0 }

        val startMinuteOfDay = startParts[0] * 60 + startParts[1]
        val endMinuteOfDay = endParts[0] * 60 + endParts[1]
        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val isOvernight = endMinuteOfDay <= startMinuteOfDay

        if (!isOvernight) {
            // Same day schedule e.g., 14:00 to 15:00
            val dayMatches = repeatDays.contains(currentDayOfWeek)
            return dayMatches && currentMinuteOfDay in startMinuteOfDay until endMinuteOfDay
        } else {
            // Overnight schedule e.g., 23:00 to 06:00
            if (currentMinuteOfDay >= startMinuteOfDay) {
                // Before midnight, check today's repeat day
                return repeatDays.contains(currentDayOfWeek)
            } else if (currentMinuteOfDay < endMinuteOfDay) {
                // After midnight, check yesterday's repeat day
                val yesterdayDayOfWeek = if (currentDayOfWeek == 1) 7 else currentDayOfWeek - 1
                return repeatDays.contains(yesterdayDayOfWeek)
            }
            return false
        }
    }

    fun getFormattedStartTime(): String {
        return try {
            val parts = startTime.split(":")
            val localTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
            localTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            startTime
        }
    }

    fun getFormattedEndTime(): String {
        return try {
            val parts = endTime.split(":")
            val localTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
            localTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            endTime
        }
    }

    fun getDurationMinutes(): Int {
        val startParts = startTime.split(":").map { it.toIntOrNull() ?: 0 }
        val endParts = endTime.split(":").map { it.toIntOrNull() ?: 0 }
        val startMins = startParts[0] * 60 + startParts[1]
        val endMins = endParts[0] * 60 + endParts[1]
        return if (endMins > startMins) {
            endMins - startMins
        } else {
            (24 * 60 - startMins) + endMins
        }
    }

    fun getFormattedDuration(): String {
        val totalMins = getDurationMinutes()
        val hours = totalMins / 60
        val mins = totalMins % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours} hrs"
            else -> "${mins} mins"
        }
    }
}
