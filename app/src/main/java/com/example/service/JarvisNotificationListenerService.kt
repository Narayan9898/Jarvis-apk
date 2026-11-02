package com.example.service

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.local.JarvisDatabase
import com.example.data.model.NotificationSummaryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JarvisNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: JarvisDatabase

    // Excluded sensitive app packages (banking, authenticators, password managers)
    private val sensitivePackagePrefixes = listOf(
        "com.google.android.apps.authenticator2",
        "com.authy",
        "com.onepassword",
        "com.lastpass",
        "com.bitwarden",
        "com.bank",
        "com.sbi",
        "com.hdfc",
        "com.icici",
        "com.axis",
        "net.one97.paytm",
        "com.phonepe",
        "com.google.android.apps.nbu.paisa.user"
    )

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getDatabase(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return

        // Skip our own notifications and sensitive security apps
        if (pkg == packageName || isSensitiveApp(pkg)) {
            return
        }

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Check if message looks like an OTP/password code, and scrub it
        if (isOtpMessage(text)) {
            return // Skip saving OTPs as mandated by privacy guidelines
        }

        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            pkg
        }

        serviceScope.launch {
            val item = NotificationSummaryItem(
                packageName = pkg,
                appName = appName,
                title = title,
                content = text,
                timestamp = System.currentTimeMillis()
            )
            database.notificationDao().insertNotification(item)
        }
    }

    private fun isSensitiveApp(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return sensitivePackagePrefixes.any { lower.contains(it) } ||
                lower.contains("bank") ||
                lower.contains("authenticator") ||
                lower.contains("password")
    }

    private fun isOtpMessage(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("otp") ||
                lower.contains("verification code") ||
                lower.contains("one-time password") ||
                lower.contains("one time password") ||
                lower.contains("security code")
    }

    companion object {
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat?.contains(pkgName) == true
        }
    }
}
