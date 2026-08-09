package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.SleepSchedule
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddEditScheduleDialog(
    initialSchedule: SleepSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, startTime: String, endTime: String, repeatDays: List<Int>, strictMode: Boolean) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialSchedule?.title ?: "Night Sleep") }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "23:00") }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "06:00") }
    var repeatDays by remember { mutableStateOf(initialSchedule?.repeatDays ?: listOf(1, 2, 3, 4, 5, 6, 7)) }
    var strictModeEnabled by remember { mutableStateOf(initialSchedule?.strictModeEnabled ?: true) }

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun formatTimeForDisplay(timeStr: String): String {
        return try {
            val parts = timeStr.split(":")
            val localTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
            localTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            timeStr
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialSchedule == null) "Add Sleep Schedule" else "Edit Sleep Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Schedule Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Schedule Name", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Pickers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Selector
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parts = startTime.split(":")
                                val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 23
                                val initialMin = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                TimePickerDialog(context, { _, hourOfDay, minute ->
                                    startTime = String.format("%02d:%02d", hourOfDay, minute)
                                }, initialHour, initialMin, false).show()
                            }
                            .testTag("start_time_picker")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Start Time", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(formatTimeForDisplay(startTime), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // End Time Selector
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parts = endTime.split(":")
                                val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 6
                                val initialMin = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                TimePickerDialog(context, { _, hourOfDay, minute ->
                                    endTime = String.format("%02d:%02d", hourOfDay, minute)
                                }, initialHour, initialMin, false).show()
                            }
                            .testTag("end_time_picker")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("End Time", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(formatTimeForDisplay(endTime), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Repeat Days Selection
                Text("Repeat Days", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val dayIndex = index + 1 // 1=Mon .. 7=Sun
                        val isSelected = repeatDays.contains(dayIndex)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF334155))
                                .clickable {
                                    repeatDays = if (isSelected) {
                                        repeatDays - dayIndex
                                    } else {
                                        (repeatDays + dayIndex).sorted()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1),
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Strict Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Strict Mode", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Reopen sleep screen if user attempts to exit", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = strictModeEnabled,
                        onCheckedChange = { strictModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier.testTag("dialog_strict_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(title, startTime, endTime, repeatDays, strictModeEnabled)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text("Save Schedule")
                    }
                }
            }
        }
    }
}
