package com.example.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.local.JarvisDatabase
import com.example.data.model.ActionPlan
import com.example.data.model.ActionStatus
import com.example.data.model.InstalledAppInfo
import com.example.data.model.MemoryItem
import com.example.data.model.ReminderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.Calendar

data class ExecutionResult(
    val success: Boolean,
    val message: String,
    val actionPlan: ActionPlan
)

class ActionExecutor(
    private val context: Context,
    private val database: JarvisDatabase
) {

    suspend fun execute(plan: ActionPlan): ExecutionResult = withContext(Dispatchers.IO) {
        val validation = ActionRegistry.validatePlan(plan)
        if (validation.isFailure) {
            val err = validation.exceptionOrNull()?.message ?: "Validation failed"
            return@withContext ExecutionResult(
                success = false,
                message = "Action blocked by safety policy: $err",
                actionPlan = plan.copy(status = ActionStatus.FAILED, resultMessage = err)
            )
        }

        try {
            val result = when (plan.actionId) {
                "open_app" -> executeOpenApp(plan)
                "search_web" -> executeSearchWeb(plan)
                "open_maps" -> executeOpenMaps(plan)
                "open_youtube" -> executeOpenYouTube(plan)
                "dial_phone" -> executeDialPhone(plan)
                "send_sms" -> executeSendSms(plan)
                "create_reminder" -> executeCreateReminder(plan)
                "set_alarm" -> executeSetAlarm(plan)
                "set_timer" -> executeSetTimer(plan)
                "open_settings" -> executeOpenSettings(plan)
                "open_camera" -> executeOpenCamera(plan)
                "open_whatsapp" -> executeOpenWhatsApp(plan)
                "save_memory" -> executeSaveMemory(plan)
                "summarize_notifications" -> executeSummarizeNotifications(plan)
                else -> ExecutionResult(
                    success = false,
                    message = "Unrecognized action identifier: ${plan.actionId}",
                    actionPlan = plan.copy(status = ActionStatus.FAILED, resultMessage = "Unrecognized action")
                )
            }
            result
        } catch (e: Exception) {
            val errMsg = "Execution error: ${e.localizedMessage ?: e.message}"
            ExecutionResult(
                success = false,
                message = errMsg,
                actionPlan = plan.copy(status = ActionStatus.FAILED, resultMessage = errMsg)
            )
        }
    }

    private fun executeOpenApp(plan: ActionPlan): ExecutionResult {
        val appNameQuery = plan.parameters["app_name"] ?: plan.parameters["package_name"] ?: ""
        val pm = context.packageManager
        val installedApps = getInstalledApplications(context)

        // Try exact package name first
        var targetPkg = plan.parameters["package_name"]
        if (targetPkg.isNullOrBlank()) {
            val match = installedApps.find { it.appName.equals(appNameQuery, ignoreCase = true) }
                ?: installedApps.find { it.appName.contains(appNameQuery, ignoreCase = true) }
                ?: installedApps.find { it.packageName.contains(appNameQuery, ignoreCase = true) }
            targetPkg = match?.packageName
        }

        if (targetPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ExecutionResult(
                    success = true,
                    message = "Opening $appNameQuery successfully.",
                    actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Opened $appNameQuery")
                )
            }
        }

        // Common apps fallback mapping
        val commonPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "settings" to "com.android.settings",
            "camera" to "com.google.android.GoogleCamera",
            "gmail" to "com.google.android.gm",
            "clock" to "com.google.android.deskclock",
            "calculator" to "com.google.android.calculator"
        )
        val normalized = appNameQuery.lowercase().trim()
        val mappedPkg = commonPackages[normalized]
        if (mappedPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(mappedPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ExecutionResult(
                    success = true,
                    message = "Launching $appNameQuery.",
                    actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Opened $appNameQuery")
                )
            }
        }

        return ExecutionResult(
            success = false,
            message = "Application '$appNameQuery' is not installed on this device.",
            actionPlan = plan.copy(status = ActionStatus.FAILED, resultMessage = "App not found")
        )
    }

    private fun executeSearchWeb(plan: ActionPlan): ExecutionResult {
        val query = plan.parameters["query"] ?: ""
        val encoded = URLEncoder.encode(query, "UTF-8")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            success = true,
            message = "Searching web for: '$query'",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Searched for $query")
        )
    }

    private fun executeOpenMaps(plan: ActionPlan): ExecutionResult {
        val destination = plan.parameters["destination"] ?: ""
        val encoded = URLEncoder.encode(destination, "UTF-8")
        val gmmIntentUri = Uri.parse("geo:0,0?q=$encoded")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mapIntent)
        return ExecutionResult(
            success = true,
            message = "Opening navigation directions to $destination.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Maps opened for $destination")
        )
    }

    private fun executeOpenYouTube(plan: ActionPlan): ExecutionResult {
        val query = plan.parameters["query"]
        val intent = if (!query.isNullOrBlank()) {
            val encoded = URLEncoder.encode(query, "UTF-8")
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded"))
        } else {
            val launch = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            launch ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return ExecutionResult(
            success = true,
            message = if (query != null) "Opening YouTube search for $query" else "Opening YouTube",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "YouTube opened")
        )
    }

    private fun executeDialPhone(plan: ActionPlan): ExecutionResult {
        val target = plan.parameters["target"] ?: ""
        var phoneNumber = target

        // If target contains letters, try resolving from Contacts
        if (target.any { it.isLetter() }) {
            val resolved = lookupContactNumber(context, target)
            if (resolved != null) {
                phoneNumber = resolved
            }
        }

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialIntent)
        return ExecutionResult(
            success = true,
            message = "Phone dialer prepared with $phoneNumber for confirmation.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Dialer opened for $phoneNumber")
        )
    }

    private fun executeSendSms(plan: ActionPlan): ExecutionResult {
        val recipient = plan.parameters["recipient"] ?: ""
        val message = plan.parameters["message"] ?: ""
        var targetNumber = recipient

        if (recipient.any { it.isLetter() }) {
            val resolved = lookupContactNumber(context, recipient)
            if (resolved != null) targetNumber = resolved
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(targetNumber)}")).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(smsIntent)
        return ExecutionResult(
            success = true,
            message = "Messaging app opened with recipient $targetNumber and your message.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "SMS composed for $targetNumber")
        )
    }

    private suspend fun executeCreateReminder(plan: ActionPlan): ExecutionResult {
        val title = plan.parameters["title"] ?: "JARVIS Reminder"
        val minutesFromNow = plan.parameters["minutes_from_now"]?.toLongOrNull() ?: 60L
        val reminderTime = System.currentTimeMillis() + (minutesFromNow * 60 * 1000)

        val reminder = ReminderItem(
            title = title,
            notes = "Scheduled via JARVIS Assistant",
            timestamp = reminderTime,
            isCompleted = false,
            isAlarm = false
        )
        database.reminderDao().insertReminder(reminder)

        // Also prepare system calendar event insertion
        try {
            val calIntent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminderTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminderTime + 30 * 60 * 1000)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(calIntent)
        } catch (_: Exception) {
            // Calendar app might not be available; reminder is safely stored in Room
        }

        return ExecutionResult(
            success = true,
            message = "Reminder '$title' created for $minutesFromNow minutes from now.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Reminder saved")
        )
    }

    private fun executeSetAlarm(plan: ActionPlan): ExecutionResult {
        val hour = plan.parameters["hour"]?.toIntOrNull() ?: 7
        val minute = plan.parameters["minute"]?.toIntOrNull() ?: 0
        val message = plan.parameters["message"] ?: "JARVIS Alarm"

        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(alarmIntent)
        return ExecutionResult(
            success = true,
            message = "Alarm set for %02d:%02d with label '$message'.".format(hour, minute),
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Alarm set")
        )
    }

    private fun executeSetTimer(plan: ActionPlan): ExecutionResult {
        val seconds = plan.parameters["seconds"]?.toIntOrNull() ?: 300
        val message = plan.parameters["message"] ?: "JARVIS Timer"

        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(timerIntent)
        return ExecutionResult(
            success = true,
            message = "Timer set for $seconds seconds.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Timer set")
        )
    }

    private fun executeOpenSettings(plan: ActionPlan): ExecutionResult {
        val settingType = plan.parameters["setting_type"]?.lowercase() ?: "general"
        val action = when (settingType) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "apps" -> Settings.ACTION_APPLICATION_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
        return ExecutionResult(
            success = true,
            message = "Opened Android $settingType settings.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Opened $settingType settings")
        )
    }

    private fun executeOpenCamera(plan: ActionPlan): ExecutionResult {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            success = true,
            message = "Camera launched.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Camera launched")
        )
    }

    private fun executeOpenWhatsApp(plan: ActionPlan): ExecutionResult {
        val phone = plan.parameters["phone_number"] ?: ""
        val text = plan.parameters["text"] ?: ""
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val uri = if (phone.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedText")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(
            success = true,
            message = "WhatsApp conversation prepared.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "WhatsApp opened")
        )
    }

    private suspend fun executeSaveMemory(plan: ActionPlan): ExecutionResult {
        val key = plan.parameters["key"] ?: "fact"
        val value = plan.parameters["value"] ?: ""
        val memory = MemoryItem(
            category = "PREFERENCE",
            keyName = key,
            valueStr = value
        )
        database.memoryDao().insertMemory(memory)
        return ExecutionResult(
            success = true,
            message = "Saved to personal memory: '$key' = '$value'.",
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = "Memory saved")
        )
    }

    private suspend fun executeSummarizeNotifications(plan: ActionPlan): ExecutionResult {
        val notifications = database.notificationDao().getRecentNotifications().first()
        val summary = if (notifications.isEmpty()) {
            "No recent notifications logged or notification permission is pending in Android Settings."
        } else {
            val count = notifications.size
            val apps = notifications.map { it.appName }.distinct().joinToString(", ")
            "You have $count recent notifications from: $apps."
        }
        return ExecutionResult(
            success = true,
            message = summary,
            actionPlan = plan.copy(status = ActionStatus.EXECUTED, resultMessage = summary)
        )
    }

    companion object {
        fun getInstalledApplications(context: Context): List<InstalledAppInfo> {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            return resolveInfos.map {
                InstalledAppInfo(
                    appName = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    isSystemApp = (it.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }.sortedBy { it.appName }
        }

        private fun lookupContactNumber(context: Context, nameQuery: String): String? {
            val uri = Uri.withAppendedPath(
                ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                Uri.encode(nameQuery)
            )
            return try {
                context.contentResolver.query(
                    uri,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
