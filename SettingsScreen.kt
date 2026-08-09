package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val strictMode by viewModel.strictModeEnabled.collectAsState()
    val foregroundProtection by viewModel.foregroundProtectionEnabled.collectAsState()
    val rebootRecovery by viewModel.rebootRecoveryEnabled.collectAsState()
    val recoveryWorker by viewModel.recoveryWorkerEnabled.collectAsState()
    val accessibilityProtection by viewModel.accessibilityProtectionEnabled.collectAsState()

    val exportJson by viewModel.exportJson.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }

    LaunchedEffect(exportJson) {
        if (exportJson != null) {
            showExportDialog = true
        }
    }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            if (importResult == true) {
                Toast.makeText(context, "Settings & Schedules imported successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to import JSON data.", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearImportResult()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure protection layers and data backup",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Protection Toggles Section
        item {
            Text(
                text = "Protection Safeguards",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF818CF8),
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingToggleItem(
                title = "Enable Strict Mode",
                subtitle = "Reopens sleep screen immediately if phone usage is attempted during sleep time",
                icon = Icons.Default.Lock,
                checked = strictMode,
                onCheckedChange = { viewModel.setStrictMode(it) },
                testTag = "setting_strict_mode_switch"
            )
        }

        item {
            SettingToggleItem(
                title = "Foreground Protection Service",
                subtitle = "Runs a persistent background shield to keep alarms and sleep monitoring alive",
                icon = Icons.Default.Shield,
                checked = foregroundProtection,
                onCheckedChange = { viewModel.setForegroundProtection(it) },
                testTag = "setting_foreground_protection_switch"
            )
        }

        item {
            SettingToggleItem(
                title = "Enable Reboot Recovery",
                subtitle = "Restores all alarms and protection automatically when your device restarts",
                icon = Icons.Default.RestartAlt,
                checked = rebootRecovery,
                onCheckedChange = { viewModel.setRebootRecovery(it) },
                testTag = "setting_reboot_recovery_switch"
            )
        }

        item {
            SettingToggleItem(
                title = "Enable Recovery Worker",
                subtitle = "Runs WorkManager checks every 15 mins to restore missing alarms or services",
                icon = Icons.Default.Work,
                checked = recoveryWorker,
                onCheckedChange = { viewModel.setRecoveryWorker(it) },
                testTag = "setting_recovery_worker_switch"
            )
        }

        item {
            SettingToggleItem(
                title = "Accessibility Protection",
                subtitle = "Detects app switching, recent apps, and multi-window attempts during sleep time",
                icon = Icons.Default.Accessibility,
                checked = accessibilityProtection,
                onCheckedChange = { viewModel.setAccessibilityProtection(it) },
                testTag = "setting_accessibility_protection_switch"
            )
        }

        // Data Backup Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF818CF8),
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_settings_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_settings_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Export Dialog
    if (showExportDialog && exportJson != null) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                viewModel.clearExportData()
            },
            title = { Text("Exported Data (JSON)") },
            text = {
                Column {
                    Text("Copy your schedules and settings backup code below:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportJson ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportJson ?: ""))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                        viewModel.clearExportData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    viewModel.clearExportData()
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Settings & Schedules") },
            text = {
                Column {
                    Text("Paste your JSON backup code below to restore schedules and preferences:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInputText.isNotBlank()) {
                            viewModel.importData(importInputText)
                            showImportDialog = false
                            importInputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Import Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6366F1)
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
