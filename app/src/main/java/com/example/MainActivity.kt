package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InstalledAppsScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkflowsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisTheme

enum class JarvisScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Core", Icons.Default.GraphicEq),
    HISTORY("History", Icons.Default.History),
    REMINDERS("Schedule", Icons.Default.Schedule),
    WORKFLOWS("Routines", Icons.Default.AutoAwesome),
    APPS("Apps", Icons.Default.Apps),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications),
    PERMISSIONS("Security", Icons.Default.Security),
    MEMORY("Memory", Icons.Default.Psychology),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: JarvisViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf(JarvisScreen.DASHBOARD) }

    // Intercept hardware/gesture back press to return to Dashboard
    if (currentScreen != JarvisScreen.DASHBOARD) {
        BackHandler {
            currentScreen = JarvisScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(
                            text = "J.A.R.V.I.S.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            color = JarvisCyan
                        )
                        Text(
                            text = " // ${currentScreen.title.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextSecondary,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisSurface,
                    titleContentColor = JarvisTextPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { currentScreen = JarvisScreen.NOTIFICATIONS },
                        modifier = Modifier.testTag("nav_top_notifications")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = if (currentScreen == JarvisScreen.NOTIFICATIONS) JarvisCyan else JarvisTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { currentScreen = JarvisScreen.PERMISSIONS },
                        modifier = Modifier.testTag("nav_top_permissions")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Dashboard",
                            tint = if (currentScreen == JarvisScreen.PERMISSIONS) JarvisCyan else JarvisTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { currentScreen = JarvisScreen.MEMORY },
                        modifier = Modifier.testTag("nav_top_memory")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Memory",
                            tint = if (currentScreen == JarvisScreen.MEMORY) JarvisCyan else JarvisTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { currentScreen = JarvisScreen.SETTINGS },
                        modifier = Modifier.testTag("nav_top_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentScreen == JarvisScreen.SETTINGS) JarvisCyan else JarvisTextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val primaryTabs = listOf(
                    JarvisScreen.DASHBOARD,
                    JarvisScreen.HISTORY,
                    JarvisScreen.REMINDERS,
                    JarvisScreen.WORKFLOWS,
                    JarvisScreen.APPS
                )

                primaryTabs.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = JarvisCyan,
                            indicatorColor = JarvisCyan,
                            unselectedIconColor = JarvisTextSecondary,
                            unselectedTextColor = JarvisTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JarvisBackground)
        ) {
            when (currentScreen) {
                JarvisScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                JarvisScreen.HISTORY -> ChatHistoryScreen(viewModel = viewModel)
                JarvisScreen.REMINDERS -> RemindersScreen(viewModel = viewModel)
                JarvisScreen.WORKFLOWS -> WorkflowsScreen(viewModel = viewModel)
                JarvisScreen.APPS -> InstalledAppsScreen()
                JarvisScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                JarvisScreen.PERMISSIONS -> PermissionsScreen()
                JarvisScreen.MEMORY -> MemoryScreen(viewModel = viewModel)
                JarvisScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
