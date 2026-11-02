package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledAppInfo
import com.example.domain.ActionExecutor
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InstalledAppsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = ActionExecutor.getInstalledApplications(context)
            isLoading = false
        }
    }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
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
                    text = "TARGET APPLICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Installed Packages (${apps.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter applications by title or package...", color = JarvisTextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = JarvisCyan) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("search_apps_input"),
            shape = RoundedCornerShape(14.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Indexing installed system packages...", color = JarvisCyan)
            }
        } else if (filteredApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No applications matching '$searchQuery'", color = JarvisTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { appInfo ->
                    AppItemRow(appInfo = appInfo, onLaunch = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    appInfo: InstalledAppInfo,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() }
            .testTag("app_item_${appInfo.packageName}"),
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(JarvisSurfaceVariant, CircleShape)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appInfo.appName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyanBright,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = appInfo.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = appInfo.packageName,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Launch",
                tint = JarvisCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
