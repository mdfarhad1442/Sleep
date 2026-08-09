package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StatisticsUiState(
    val totalMinutesLogged: Int = 0,
    val totalSessionsCompleted: Int = 0,
    val complianceRate: Int = 0,
    val averageHoursPerSession: Float = 0f,
    val weeklyHoursData: List<Pair<String, Float>> = emptyList()
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val db = SleepDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        val minutes = prefs.getTotalMinutesLogged()
        val sessions = prefs.getSleepSessionsCompleted()
        val compliance = prefs.getComplianceRate()
        val avgHours = if (sessions > 0) (minutes / 60f) / sessions else 0f

        // Mock/Sample weekly distribution based on logged data for visual charts
        val weeklyData = listOf(
            "Mon" to (if (sessions > 0) 7.5f else 0f),
            "Tue" to (if (sessions > 0) 8.0f else 0f),
            "Wed" to (if (sessions > 0) 6.8f else 0f),
            "Thu" to (if (sessions > 0) 7.2f else 0f),
            "Fri" to (if (sessions > 0) 8.5f else 0f),
            "Sat" to (if (sessions > 0) 9.0f else 0f),
            "Sun" to (if (sessions > 0) 8.2f else 0f)
        )

        _uiState.value = StatisticsUiState(
            totalMinutesLogged = minutes,
            totalSessionsCompleted = sessions,
            complianceRate = compliance,
            averageHoursPerSession = avgHours,
            weeklyHoursData = weeklyData
        )
    }
}
