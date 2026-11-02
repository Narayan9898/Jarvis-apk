package com.example.domain

import com.example.data.model.ActionPlan
import com.example.data.model.RiskLevel

data class RegisteredAction(
    val actionId: String,
    val name: String,
    val description: String,
    val requiredPermission: String? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val requiredParameters: List<String> = emptyList(),
    val optionalParameters: List<String> = emptyList()
)

object ActionRegistry {
    val actions = mapOf(
        "open_app" to RegisteredAction(
            actionId = "open_app",
            name = "Open Application",
            description = "Launch an installed Android application by name or package",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("app_name"),
            optionalParameters = listOf("package_name")
        ),
        "search_web" to RegisteredAction(
            actionId = "search_web",
            name = "Search Internet",
            description = "Perform a web search query via browser",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("query")
        ),
        "open_maps" to RegisteredAction(
            actionId = "open_maps",
            name = "Open Maps & Navigation",
            description = "Show directions or location on Google Maps",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("destination")
        ),
        "open_youtube" to RegisteredAction(
            actionId = "open_youtube",
            name = "Open YouTube",
            description = "Search and play a video on YouTube",
            riskLevel = RiskLevel.LOW,
            optionalParameters = listOf("query")
        ),
        "dial_phone" to RegisteredAction(
            actionId = "dial_phone",
            name = "Prepare Phone Call",
            description = "Open the phone dialer with specified number or contact",
            riskLevel = RiskLevel.MEDIUM,
            requiredParameters = listOf("target")
        ),
        "send_sms" to RegisteredAction(
            actionId = "send_sms",
            name = "Compose SMS Message",
            description = "Open system messaging app with recipient and prefilled message",
            riskLevel = RiskLevel.MEDIUM,
            requiredParameters = listOf("recipient", "message")
        ),
        "create_reminder" to RegisteredAction(
            actionId = "create_reminder",
            name = "Create Reminder",
            description = "Add a scheduled reminder in JARVIS memory",
            riskLevel = RiskLevel.MEDIUM,
            requiredParameters = listOf("title"),
            optionalParameters = listOf("minutes_from_now", "date_time")
        ),
        "set_alarm" to RegisteredAction(
            actionId = "set_alarm",
            name = "Set System Alarm",
            description = "Set an alarm using the Android Clock service",
            riskLevel = RiskLevel.MEDIUM,
            requiredParameters = listOf("hour", "minute"),
            optionalParameters = listOf("message")
        ),
        "set_timer" to RegisteredAction(
            actionId = "set_timer",
            name = "Set Timer",
            description = "Start a countdown timer",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("seconds"),
            optionalParameters = listOf("message")
        ),
        "open_settings" to RegisteredAction(
            actionId = "open_settings",
            name = "Open System Settings",
            description = "Open Wi-Fi, Bluetooth, Battery, Display, or Accessibility settings",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("setting_type")
        ),
        "open_camera" to RegisteredAction(
            actionId = "open_camera",
            name = "Open Camera",
            description = "Launch camera app for capturing photo",
            riskLevel = RiskLevel.LOW
        ),
        "open_whatsapp" to RegisteredAction(
            actionId = "open_whatsapp",
            name = "Open WhatsApp Chat",
            description = "Open WhatsApp conversation or share text",
            riskLevel = RiskLevel.MEDIUM,
            optionalParameters = listOf("phone_number", "text")
        ),
        "save_memory" to RegisteredAction(
            actionId = "save_memory",
            name = "Remember Preference",
            description = "Save user preference or fact to personal memory",
            riskLevel = RiskLevel.LOW,
            requiredParameters = listOf("key", "value")
        ),
        "summarize_notifications" to RegisteredAction(
            actionId = "summarize_notifications",
            name = "Summarize Notifications",
            description = "Read and summarize recent permitted notifications",
            riskLevel = RiskLevel.LOW
        )
    )

    fun getAction(actionId: String): RegisteredAction? = actions[actionId]

    fun validatePlan(plan: ActionPlan): Result<ActionPlan> {
        val registered = actions[plan.actionId]
            ?: return Result.failure(IllegalArgumentException("Action '${plan.actionId}' is not permitted in Action Registry."))

        for (param in registered.requiredParameters) {
            val value = plan.parameters[param]
            if (value.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Missing required parameter '$param' for action '${registered.name}'"))
            }
        }

        val requiresConfirm = plan.requiresConfirmation || registered.riskLevel != RiskLevel.LOW
        return Result.success(
            plan.copy(
                title = registered.name,
                description = registered.description,
                riskLevel = registered.riskLevel,
                requiresConfirmation = requiresConfirm
            )
        )
    }
}
