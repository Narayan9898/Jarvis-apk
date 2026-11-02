package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.JarvisAssistantState
import com.example.ui.JarvisViewModel
import com.example.ui.components.ActionConfirmationCard
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisError
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun DashboardScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.assistantState.collectAsState()
    val audioLevel by viewModel.audioWaveform.collectAsState()
    val activeAction by viewModel.activeActionPlan.collectAsState()
    val bannerText by viewModel.statusBannerText.collectAsState()
    val language by viewModel.languagePref.collectAsState()
    val offlineMode by viewModel.offlineModePref.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lastResponse = chatMessages.lastOrNull { it.sender != com.example.data.model.MessageSender.USER }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MARK VII SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "JARVIS ONLINE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = JarvisTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (offlineMode) JarvisSurfaceVariant else JarvisCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (offlineMode) "OFFLINE NLU" else "GEMINI CLOUD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (offlineMode) JarvisTextSecondary else JarvisCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(JarvisSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = language.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arc Reactor Visualizer
            ArcReactorVisualizer(
                state = state,
                audioLevel = audioLevel,
                size = 200.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // State Badge Indicator
            Box(
                modifier = Modifier
                    .background(
                        color = when (state) {
                            JarvisAssistantState.LISTENING -> JarvisCyan.copy(alpha = 0.2f)
                            JarvisAssistantState.THINKING -> Color(0xFF7C4DFF).copy(alpha = 0.2f)
                            JarvisAssistantState.SPEAKING -> Color(0xFF00E676).copy(alpha = 0.2f)
                            JarvisAssistantState.EXECUTING -> Color(0xFFFFAB00).copy(alpha = 0.2f)
                            JarvisAssistantState.IDLE -> JarvisSurfaceVariant
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = when (state) {
                            JarvisAssistantState.LISTENING -> JarvisCyan
                            JarvisAssistantState.THINKING -> Color(0xFF7C4DFF)
                            JarvisAssistantState.SPEAKING -> Color(0xFF00E676)
                            JarvisAssistantState.EXECUTING -> Color(0xFFFFAB00)
                            JarvisAssistantState.IDLE -> JarvisBorderGlow
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = when (state) {
                        JarvisAssistantState.LISTENING -> JarvisCyan
                        JarvisAssistantState.THINKING -> Color(0xFF7C4DFF)
                        JarvisAssistantState.SPEAKING -> Color(0xFF00E676)
                        JarvisAssistantState.EXECUTING -> Color(0xFFFFAB00)
                        JarvisAssistantState.IDLE -> JarvisTextSecondary
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waveform Audio Activity
            AudioWaveformVisualizer(
                state = state,
                audioLevel = audioLevel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Status Banner if present
            AnimatedVisibility(
                visible = bannerText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = bannerText ?: "",
                    fontSize = 13.sp,
                    color = JarvisCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // Pending Action Confirmation Card
            if (activeAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ActionConfirmationCard(
                    plan = activeAction!!,
                    onConfirm = { viewModel.confirmActiveAction() },
                    onCancel = { viewModel.cancelActiveAction() }
                )
            }

            // Latest Response Card
            if (lastResponse != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, JarvisBorderGlow, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JARVIS INTELLIGENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                letterSpacing = 1.sp
                            )
                            if (state == JarvisAssistantState.SPEAKING) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speaking",
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Vocalizing",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E676)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastResponse.text,
                            fontSize = 14.sp,
                            color = JarvisTextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Command Chips (English, Telugu, Hindi)
            Text(
                text = "SUGGESTED PROTOCOLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = JarvisTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Hey JARVIS, open YouTube",
                    "జార్విస్, YouTube ఓపెన్ చేయి",
                    "Set alarm for 6 AM",
                    "Good morning JARVIS",
                    "Summarize notifications",
                    "Open Wi-Fi settings",
                    "రేపు ఉదయం అలారం పెట్టు",
                    "Directions to Hyderabad"
                )

                suggestions.forEach { command ->
                    SuggestionChip(
                        onClick = { viewModel.sendTextCommand(command) },
                        label = { Text(command, fontSize = 12.sp, color = JarvisTextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = JarvisSurfaceVariant
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = JarvisBorderGlow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Mic & Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel / Stop button if speaking or listening
                if (state == JarvisAssistantState.SPEAKING || state == JarvisAssistantState.LISTENING) {
                    IconButton(
                        onClick = {
                            if (state == JarvisAssistantState.SPEAKING) viewModel.stopSpeaking()
                            if (state == JarvisAssistantState.LISTENING) viewModel.stopListening()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(JarvisError.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, JarvisError, CircleShape)
                            .testTag("stop_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = JarvisError
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }

                // Main Push-to-Talk Microphone FAB
                FloatingActionButton(
                    onClick = {
                        if (state == JarvisAssistantState.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startListening()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("microphone_button"),
                    containerColor = if (state == JarvisAssistantState.LISTENING) JarvisError else JarvisCyan,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (state == JarvisAssistantState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Enter command (English, తెలుగు, हिंदी)...", fontSize = 13.sp, color = JarvisTextSecondary) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("text_command_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarvisSurface,
                        unfocusedContainerColor = JarvisSurface,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendTextCommand(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
