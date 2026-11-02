package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderItem
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCHEDULE & ALARMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Active Reminders (${reminders.count { !it.isCompleted }})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "No reminders",
                            tint = JarvisCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Scheduled Directives",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Say 'Hey JARVIS, set an alarm for 6 AM' or tap + to create one manually.",
                            fontSize = 13.sp,
                            color = JarvisTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminderCompleted(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_reminder_fab"),
            containerColor = JarvisCyan,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Reminder")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var minutesStr by remember { mutableStateOf("30") }
        var isAlarm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create System Directive", color = JarvisTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task / Alarm Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = minutesStr,
                        onValueChange = { minutesStr = it },
                        label = { Text("Minutes from now") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trigger System Alarm Clock", color = JarvisTextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = isAlarm,
                            onCheckedChange = { isAlarm = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = minutesStr.toLongOrNull() ?: 30L
                        if (title.isNotBlank()) {
                            viewModel.addReminder(title, mins, isAlarm)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(reminder.timestamp) { dateFormat.format(Date(reminder.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (reminder.isCompleted) Color(0xFF00E676) else JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = reminder.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCompleted) JarvisTextSecondary else JarvisTextPrimary,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (reminder.isAlarm) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarm",
                                tint = JarvisCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = JarvisError.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
