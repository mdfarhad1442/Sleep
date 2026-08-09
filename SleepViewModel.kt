package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SleepDatabase
import com.example.data.SleepRepository
import com.example.data.SleepSchedule
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SleepDatabase.getDatabase(application)
    private val prefs = UserPreferences(application)
    val repository = SleepRepository(db.sleepScheduleDao(), prefs)

    val allSchedules: StateFlow<List<SleepSchedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val onboardingCompleted: StateFlow<Boolean> = repository.userPreferences.onboardingCompleted

    fun setOnboardingCompleted(completed: Boolean) {
        repository.userPreferences.setOnboardingCompleted(completed)
    }

    fun addSchedule(
        title: String,
        startTime: String,
        endTime: String,
        repeatDays: List<Int>,
        strictModeEnabled: Boolean
    ) {
        viewModelScope.launch {
            val schedule = SleepSchedule(
                title = title.ifBlank { "Sleep Schedule" },
                startTime = startTime,
                endTime = endTime,
                repeatDays = repeatDays,
                strictModeEnabled = strictModeEnabled,
                enabled = true
            )
            val id = repository.insertSchedule(schedule)
            val newSchedule = schedule.copy(id = id.toInt())
            AlarmScheduler.scheduleAlarmsForSchedule(getApplication(), newSchedule)

            if (prefs.getForegroundProtectionEnabled()) {
                SleepProtectionService.startService(getApplication())
            }
        }
    }

    fun updateSchedule(schedule: SleepSchedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
            if (schedule.enabled) {
                AlarmScheduler.scheduleAlarmsForSchedule(getApplication(), schedule)
            } else {
                AlarmScheduler.cancelAlarmsForSchedule(getApplication(), schedule)
            }
        }
    }

    fun deleteSchedule(schedule: SleepSchedule) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarmsForSchedule(getApplication(), schedule)
            repository.deleteSchedule(schedule)
        }
    }

    fun toggleScheduleEnabled(schedule: SleepSchedule, enabled: Boolean) {
        viewModelScope.launch {
            val updated = schedule.copy(enabled = enabled)
            repository.updateSchedule(updated)
            if (enabled) {
                AlarmScheduler.scheduleAlarmsForSchedule(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarmsForSchedule(getApplication(), updated)
            }
        }
    }
}
