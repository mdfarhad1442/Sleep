package com.example.data

import kotlinx.coroutines.flow.Flow

class SleepRepository(
    private val dao: SleepScheduleDao,
    val userPreferences: UserPreferences
) {
    val allSchedules: Flow<List<SleepSchedule>> = dao.getAllSchedules()
    val enabledSchedules: Flow<List<SleepSchedule>> = dao.getEnabledSchedules()

    suspend fun getEnabledSchedulesSync(): List<SleepSchedule> = dao.getEnabledSchedulesSync()
    suspend fun getAllSchedulesSync(): List<SleepSchedule> = dao.getAllSchedulesSync()
    suspend fun getScheduleById(id: Int): SleepSchedule? = dao.getScheduleById(id)

    suspend fun insertSchedule(schedule: SleepSchedule): Long {
        val id = dao.insertSchedule(schedule)
        userPreferences.incrementScheduledCount()
        return id
    }

    suspend fun updateSchedule(schedule: SleepSchedule) {
        dao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: SleepSchedule) {
        dao.deleteSchedule(schedule)
    }

    suspend fun deleteScheduleById(id: Int) {
        dao.deleteScheduleById(id)
    }

    suspend fun setScheduleEnabled(id: Int, enabled: Boolean) {
        dao.setScheduleEnabled(id, enabled)
    }
}
