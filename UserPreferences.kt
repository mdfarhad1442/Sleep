package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sleep_hour_prefs", Context.MODE_PRIVATE)

    private val _strictModeEnabled = MutableStateFlow(getStrictModeEnabled())
    val strictModeEnabled: StateFlow<Boolean> = _strictModeEnabled.asStateFlow()

    private val _foregroundProtectionEnabled = MutableStateFlow(getForegroundProtectionEnabled())
    val foregroundProtectionEnabled: StateFlow<Boolean> = _foregroundProtectionEnabled.asStateFlow()

    private val _rebootRecoveryEnabled = MutableStateFlow(getRebootRecoveryEnabled())
    val rebootRecoveryEnabled: StateFlow<Boolean> = _rebootRecoveryEnabled.asStateFlow()

    private val _recoveryWorkerEnabled = MutableStateFlow(getRecoveryWorkerEnabled())
    val recoveryWorkerEnabled: StateFlow<Boolean> = _recoveryWorkerEnabled.asStateFlow()

    private val _accessibilityProtectionEnabled = MutableStateFlow(getAccessibilityProtectionEnabled())
    val accessibilityProtectionEnabled: StateFlow<Boolean> = _accessibilityProtectionEnabled.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(isOnboardingCompleted())
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setStrictModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
        _strictModeEnabled.value = enabled
    }

    fun getStrictModeEnabled(): Boolean = prefs.getBoolean(KEY_STRICT_MODE, true)

    fun setForegroundProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOREGROUND_PROTECTION, enabled).apply()
        _foregroundProtectionEnabled.value = enabled
    }

    fun getForegroundProtectionEnabled(): Boolean = prefs.getBoolean(KEY_FOREGROUND_PROTECTION, true)

    fun setRebootRecoveryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REBOOT_RECOVERY, enabled).apply()
        _rebootRecoveryEnabled.value = enabled
    }

    fun getRebootRecoveryEnabled(): Boolean = prefs.getBoolean(KEY_REBOOT_RECOVERY, true)

    fun setRecoveryWorkerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RECOVERY_WORKER, enabled).apply()
        _recoveryWorkerEnabled.value = enabled
    }

    fun getRecoveryWorkerEnabled(): Boolean = prefs.getBoolean(KEY_RECOVERY_WORKER, true)

    fun setAccessibilityProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_PROTECTION, enabled).apply()
        _accessibilityProtectionEnabled.value = enabled
    }

    fun getAccessibilityProtectionEnabled(): Boolean = prefs.getBoolean(KEY_ACCESSIBILITY_PROTECTION, true)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    // Stats tracking
    fun incrementSleepSessionsCompleted() {
        val current = prefs.getInt(KEY_SESSIONS_COMPLETED, 0)
        prefs.edit().putInt(KEY_SESSIONS_COMPLETED, current + 1).apply()
    }

    fun getSleepSessionsCompleted(): Int = prefs.getInt(KEY_SESSIONS_COMPLETED, 0)

    fun addSleepMinutesLogged(minutes: Int) {
        val current = prefs.getInt(KEY_TOTAL_MINUTES_LOGGED, 0)
        prefs.edit().putInt(KEY_TOTAL_MINUTES_LOGGED, current + minutes).apply()
    }

    fun getTotalMinutesLogged(): Int = prefs.getInt(KEY_TOTAL_MINUTES_LOGGED, 0)

    fun getComplianceRate(): Int {
        val total = prefs.getInt(KEY_TOTAL_SCHEDULED_COUNT, 1)
        val completed = prefs.getInt(KEY_SESSIONS_COMPLETED, 1)
        return ((completed.toFloat() / total.coerceAtLeast(1).toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    fun incrementScheduledCount() {
        val current = prefs.getInt(KEY_TOTAL_SCHEDULED_COUNT, 0)
        prefs.edit().putInt(KEY_TOTAL_SCHEDULED_COUNT, current + 1).apply()
    }

    companion object {
        private const val KEY_STRICT_MODE = "strict_mode_enabled"
        private const val KEY_FOREGROUND_PROTECTION = "foreground_protection_enabled"
        private const val KEY_REBOOT_RECOVERY = "reboot_recovery_enabled"
        private const val KEY_RECOVERY_WORKER = "recovery_worker_enabled"
        private const val KEY_ACCESSIBILITY_PROTECTION = "accessibility_protection_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        private const val KEY_SESSIONS_COMPLETED = "sessions_completed"
        private const val KEY_TOTAL_MINUTES_LOGGED = "total_minutes_logged"
        private const val KEY_TOTAL_SCHEDULED_COUNT = "total_scheduled_count"
    }
}
