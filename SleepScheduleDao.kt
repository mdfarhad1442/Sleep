package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepScheduleDao {
    @Query("SELECT * FROM sleep_schedules ORDER BY createdAt DESC")
    fun getAllSchedules(): Flow<List<SleepSchedule>>

    @Query("SELECT * FROM sleep_schedules WHERE enabled = 1")
    fun getEnabledSchedules(): Flow<List<SleepSchedule>>

    @Query("SELECT * FROM sleep_schedules WHERE enabled = 1")
    suspend fun getEnabledSchedulesSync(): List<SleepSchedule>

    @Query("SELECT * FROM sleep_schedules")
    suspend fun getAllSchedulesSync(): List<SleepSchedule>

    @Query("SELECT * FROM sleep_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Int): SleepSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: SleepSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: SleepSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: SleepSchedule)

    @Query("DELETE FROM sleep_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Int)

    @Query("UPDATE sleep_schedules SET enabled = :enabled WHERE id = :id")
    suspend fun setScheduleEnabled(id: Int, enabled: Boolean)
}
