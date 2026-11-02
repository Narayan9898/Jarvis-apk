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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionPlan
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
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
                    text = "TRANSLOG ARCHIVES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Conversation History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
            }

            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = JarvisTextSecondary
                    )
                }
            }
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "No Messages",
                        tint = JarvisCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memory bank empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Speak or type a command on the dashboard to start interacting with JARVIS.",
                        fontSize = 13.sp,
                        color = JarvisTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Erase Conversation History?", color = JarvisTextPrimary) },
            text = { Text("This will permanently remove stored conversation messages from local memory.", color = JarvisTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = JarvisError)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val actionPlan = remember(message.actionPlanJson) {
        message.actionPlanJson?.let { ActionPlan.fromJson(it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when {
            isUser -> Alignment.End
            isSystem -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isSystem) 0.95f else 0.85f)
                .background(
                    color = when {
                        isUser -> JarvisCyan.copy(alpha = 0.15f)
                        isSystem -> JarvisSurfaceVariant
                        else -> JarvisSurface
                    },
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isUser -> JarvisCyan.copy(alpha = 0.4f)
                        isSystem -> JarvisBorderGlow
                        else -> JarvisBorderGlow
                    },
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isUser -> "YOU"
                            isSystem -> "AUDIT LOG"
                            else -> "JARVIS"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) JarvisCyanBright else JarvisCyan,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = JarvisTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = JarvisTextPrimary,
                    lineHeight = 20.sp
                )

                if (actionPlan != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisBackground, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTION: ${actionPlan.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JarvisCyan
                            )
                            Box(
                                modifier = Modifier
                                    .background(JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = actionPlan.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
