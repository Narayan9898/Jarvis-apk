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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.languagePref.collectAsState()
    val voiceFeedback by viewModel.voiceFeedbackPref.collectAsState()
    val offlineMode by viewModel.offlineModePref.collectAsState()
    val savedApiKey by viewModel.geminiApiKeyPref.collectAsState()
    val savedProxyUrl by viewModel.backendProxyUrlPref.collectAsState()

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var proxyUrlInput by remember(savedProxyUrl) { mutableStateOf(savedProxyUrl) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(scrollState)
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
                    text = "SYSTEM CONFIGURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Core Preferences & AI Engine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Language Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assistant Language", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select your primary dialect or enable multi-lingual auto-detection.", fontSize = 12.sp, color = JarvisTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf("auto" to "Auto-Detect", "en" to "English", "te" to "తెలుగు", "hi" to "हिंदी")
                    languages.forEach { (code, label) ->
                        FilterChip(
                            selected = language == code,
                            onClick = { viewModel.setLanguage(code) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JarvisCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Speech Output & Voice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Speech & Vocalization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Spoken Audio Feedback", fontSize = 14.sp, color = JarvisTextPrimary)
                        Text("JARVIS speaks answers aloud using Android TTS", fontSize = 11.sp, color = JarvisTextSecondary)
                    }
                    Switch(
                        checked = voiceFeedback,
                        onCheckedChange = { viewModel.setVoiceFeedback(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Speech Velocity: ${"%.1f".format(speechRate)}x", fontSize = 13.sp, color = JarvisTextPrimary)
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        viewModel.setSpeechRate(it)
                    },
                    valueRange = 0.6f..1.6f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. AI Model & Backend Security
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Gemini & Backend Gateway", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Force Offline Mode", fontSize = 14.sp, color = JarvisTextPrimary)
                        Text("Use strictly device-local rule-based NLU without cloud APIs", fontSize = 11.sp, color = JarvisTextSecondary)
                    }
                    Switch(
                        checked = offlineMode,
                        onCheckedChange = { viewModel.setOfflineMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Gemini API Key Override") },
                    placeholder = { Text("Configured automatically via AI Studio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = proxyUrlInput,
                    onValueChange = { proxyUrlInput = it },
                    label = { Text("Production Backend Proxy URL") },
                    placeholder = { Text("e.g. https://jarvis-backend.example.com/api/chat") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backend_proxy_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.setGeminiApiKey(apiKeyInput.trim())
                        viewModel.setBackendProxyUrl(proxyUrlInput.trim())
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Gateway Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Security & Compliance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enterprise Security Architecture", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = JarvisCyanBright)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Controlled Action Registry: AI never executes arbitrary scripts or shell commands.\n" +
                            "• Risk-Based Confirmation: Sensitive actions require explicit biometric/user confirmation.\n" +
                            "• Zero-Leakage Notifications: Excludes banking apps, password managers, and OTPs automatically.\n" +
                            "• Local Data Persistence: Conversation and memories are held locally in encrypted Room SQLite storage.",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. About JARVIS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JARVIS AI • Version 1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JarvisTextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Advanced Personal Android Assistant inspired by fictional JARVIS. Engineered with Clean Architecture, MVVM, Jetpack Compose, Room, and Gemini NLU.",
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
