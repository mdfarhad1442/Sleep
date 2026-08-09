package com.example.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SleepDatabase
import com.example.data.SleepSchedule
import com.example.data.UserPreferences
import com.example.receiver.SleepEndReceiver
import com.example.ui.theme.SleepHourTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SleepActivity : ComponentActivity() {

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SleepEndReceiver.ACTION_FINISH_SLEEP_ACTIVITY) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn screen on and show over keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        makeImmersive()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                finishReceiver,
                IntentFilter(SleepEndReceiver.ACTION_FINISH_SLEEP_ACTIVITY),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(finishReceiver, IntentFilter(SleepEndReceiver.ACTION_FINISH_SLEEP_ACTIVITY))
        }

        setContent {
            SleepHourTheme(darkTheme = true) {
                SleepModeContent(
                    onExitRequested = {
                        finish()
                    }
                )
            }
        }
    }

    private fun makeImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        makeImmersive()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(finishReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }
}

@Composable
fun SleepModeContent(onExitRequested: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val isStrictMode = prefs.getStrictModeEnabled()

    var activeSchedule by remember { mutableStateOf<SleepSchedule?>(null) }
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var countdownString by remember { mutableStateOf("") }
    var showEmergencyExitDialog by remember { mutableStateOf(false) }

    val motivationalMessages = remember {
        listOf(
            "Rest is the foundation of energy and vitality.",
            "Your mind heals while you sleep. Put the phone down.",
            "Disconnect from the noise. Reconnect with yourself.",
            "A well-rested tomorrow starts with a peaceful tonight.",
            "Dream big, sleep deep."
        )
    }

    val selectedQuote = remember { motivationalMessages.random() }

    LaunchedEffect(Unit) {
        val db = SleepDatabase.getDatabase(context)
        val enabledList = db.sleepScheduleDao().getEnabledSchedulesSync()
        activeSchedule = enabledList.firstOrNull { it.isCurrentlyActive() }

        while (true) {
            val now = LocalDateTime.now()
            currentTimeString = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
            currentDateString = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))

            // Calculate countdown until end time
            val sched = activeSchedule
            if (sched != null) {
                val endParts = sched.endTime.split(":").map { it.toIntOrNull() ?: 0 }
                val calendar = Calendar.getInstance()
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, endParts[0])
                    set(Calendar.MINUTE, endParts[1])
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= calendar.timeInMillis) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                val diffMillis = targetCal.timeInMillis - calendar.timeInMillis
                if (diffMillis > 0) {
                    val hrs = diffMillis / (1000 * 60 * 60)
                    val mins = (diffMillis / (1000 * 60)) % 60
                    val secs = (diffMillis / 1000) % 60
                    countdownString = String.format("%02dh %02dm %02ds", hrs, mins, secs)
                } else {
                    countdownString = "Ending soon..."
                }
            } else {
                countdownString = "Active Session"
            }

            delay(1000)
        }
    }

    // Handle back button press in strict mode
    BackHandler(enabled = true) {
        if (!isStrictMode) {
            onExitRequested()
        } else {
            showEmergencyExitDialog = true
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070B19),
            Color(0xFF0F172A),
            Color(0xFF1E1B4B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Badge header
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF818CF8).copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Bedtime Icon",
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SLEEP TIME ACTIVE",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFA5B4FC),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Large Digital Clock
            Text(
                text = currentTimeString.ifEmpty { "--:--:--" },
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("sleep_clock")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentDateString,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Countdown Timer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIME REMAINING UNTIL END",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = countdownString.ifEmpty { "Calculating..." },
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF818CF8),
                        fontWeight = FontWeight.Bold
                    )
                    if (activeSchedule != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activeSchedule?.getFormattedStartTime()} - ${activeSchedule?.getFormattedEndTime()} (${activeSchedule?.title})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Motivational Quote
            Text(
                text = "\"$selectedQuote\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Strict mode / exit control
            if (!isStrictMode) {
                OutlinedButton(
                    onClick = onExitRequested,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.testTag("exit_sleep_button")
                ) {
                    Text("End Sleep Mode")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Strict Mode Active",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Strict Mode Protection Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF59E0B)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showEmergencyExitDialog = true },
                    modifier = Modifier.testTag("emergency_exit_button")
                ) {
                    Text("Emergency Unlock", color = Color(0xFF94A3B8))
                }
            }
        }
    }

    if (showEmergencyExitDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyExitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency Unlock",
                    tint = Color(0xFFEF4444)
                )
            },
            title = { Text("Emergency Override") },
            text = {
                Text("Strict Mode is active to help you protect your sleep habits. Are you sure you need to exit Sleep Mode early?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyExitDialog = false
                        onExitRequested()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Unlock & Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyExitDialog = false }) {
                    Text("Keep Sleeping")
                }
            }
        )
    }
}
