package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.JarvisNotificationListenerService
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarning

data class PermissionItemData(
    val title: String,
    val permission: String,
    val icon: ImageVector,
    val whyNeeded: String,
    val dataAccessed: String,
    val featureRequiring: String,
    val isGranted: Boolean
)

@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }

    val singlePermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTrigger++
    }

    val permissionsList = remember(refreshTrigger) {
        listOf(
            PermissionItemData(
                title = "Microphone Access",
                permission = Manifest.permission.RECORD_AUDIO,
                icon = Icons.Default.Mic,
                whyNeeded = "Required to convert your voice commands into speech-to-text in Telugu, Hindi, and English.",
                dataAccessed = "Audio stream strictly during active push-to-talk listening sessions.",
                featureRequiring = "Voice Assistant & Push-to-Talk engine.",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            ),
            PermissionItemData(
                title = "Contacts Access",
                permission = Manifest.permission.READ_CONTACTS,
                icon = Icons.Default.Contacts,
                whyNeeded = "Required to match spoken names (e.g. 'Call Father' or 'Message Ravi') to device phone numbers.",
                dataAccessed = "Contact names and phone numbers stored on your device.",
                featureRequiring = "Phone call preparation and SMS recipient resolution.",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            ),
            PermissionItemData(
                title = "Calendar Access",
                permission = Manifest.permission.READ_CALENDAR,
                icon = Icons.Default.CalendarMonth,
                whyNeeded = "Allows JARVIS to schedule upcoming appointments and reminders directly to your device calendar.",
                dataAccessed = "Calendar event titles, dates, and times.",
                featureRequiring = "Schedule management and morning routine briefs.",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
            ),
            PermissionItemData(
                title = "Camera Access",
                permission = Manifest.permission.CAMERA,
                icon = Icons.Default.CameraAlt,
                whyNeeded = "Enables launching the camera to capture images upon user voice request.",
                dataAccessed = "Device camera hardware strictly upon request.",
                featureRequiring = "Camera launch & vision assistant.",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            )
        )
    }

    val isNotificationListenerGranted = remember(refreshTrigger) {
        JarvisNotificationListenerService.isNotificationServiceEnabled(context)
    }

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
                    text = "SECURITY AUDIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Permissions Dashboard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
            }
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("App Settings", fontSize = 11.sp, color = JarvisCyan)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(permissionsList.size) { index ->
                val item = permissionsList[index]
                PermissionDetailCard(
                    item = item,
                    onRequest = { singlePermLauncher.launch(item.permission) }
                )
            }

            // Notification Access Special Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification Access",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Notification Assistant Access",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                            }
                            StatusPill(isGranted = isNotificationListenerGranted)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Why needed: Summarizes non-sensitive alerts and identifies calendar events.",
                            fontSize = 12.sp,
                            color = JarvisTextSecondary
                        )
                        Text(
                            text = "Security guarantee: OTPs, passwords, and banking apps are excluded entirely.",
                            fontSize = 11.sp,
                            color = JarvisCyanBright
                        )

                        if (!isNotificationListenerGranted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Grant", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Configure Notification Access")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDetailCard(
    item: PermissionItemData,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }
                StatusPill(isGranted = item.isGranted)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Purpose: ${item.whyNeeded}", fontSize = 12.sp, color = JarvisTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Data accessed: ${item.dataAccessed}", fontSize = 11.sp, color = JarvisTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Feature: ${item.featureRequiring}", fontSize = 11.sp, color = JarvisCyan)

            if (!item.isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Text("Grant Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusPill(isGranted: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isGranted) Color(0xFF00E676).copy(alpha = 0.2f) else JarvisWarning.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF00E676) else JarvisWarning,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "GRANTED" else "NOT GRANTED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) Color(0xFF00E676) else JarvisWarning
            )
        }
    }
}
