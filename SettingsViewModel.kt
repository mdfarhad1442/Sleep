package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SleepDatabase
import com.example.data.UserPreferences
import com.example.service.SleepProtectionService
import com.example.util.ExportImportHelper
import com.example.worker.RecoveryWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SleepDatabase.getDatabase(application)
    val prefs = UserPreferences(application)

    val strictModeEnabled: StateFlow<Boolean> = prefs.strictModeEnabled
    val foregroundProtectionEnabled: StateFlow<Boolean> = prefs.foregroundProtectionEnabled
    val rebootRecoveryEnabled: StateFlow<Boolean> = prefs.rebootRecoveryEnabled
    val recoveryWorkerEnabled: StateFlow<Boolean> = prefs.recoveryWorkerEnabled
    val accessibilityProtectionEnabled: StateFlow<Boolean> = prefs.accessibilityProtectionEnabled

    private val _exportJson = MutableStateFlow<String?>(null)
    val exportJson: StateFlow<String?> = _exportJson.asStateFlow()

    private val _importResult = MutableStateFlow<Boolean?>(null)
    val importResult: StateFlow<Boolean?> = _importResult.asStateFlow()

    fun setStrictMode(enabled: Boolean) {
        prefs.setStrictModeEnabled(enabled)
    }

    fun setForegroundProtection(enabled: Boolean) {
        prefs.setForegroundProtectionEnabled(enabled)
        if (enabled) {
            SleepProtectionService.startService(getApplication())
        } else {
            SleepProtectionService.stopService(getApplication())
        }
    }

    fun setRebootRecovery(enabled: Boolean) {
        prefs.setRebootRecoveryEnabled(enabled)
    }

    fun setRecoveryWorker(enabled: Boolean) {
        prefs.setRecoveryWorkerEnabled(enabled)
        if (enabled) {
            RecoveryWorker.schedulePeriodicRecovery(getApplication())
        } else {
            RecoveryWorker.cancelPeriodicRecovery(getApplication())
        }
    }

    fun setAccessibilityProtection(enabled: Boolean) {
        prefs.setAccessibilityProtectionEnabled(enabled)
    }

    fun exportData() {
        viewModelScope.launch {
            val json = ExportImportHelper.exportDataToJson(db.sleepScheduleDao(), prefs)
            _exportJson.value = json
        }
    }

    fun clearExportData() {
        _exportJson.value = null
    }

    fun importData(jsonStr: String) {
        viewModelScope.launch {
            val success = ExportImportHelper.importDataFromJson(jsonStr, db.sleepScheduleDao(), prefs)
            _importResult.value = success
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
