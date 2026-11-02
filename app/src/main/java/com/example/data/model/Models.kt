package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

enum class MessageSender {
    USER, JARVIS, SYSTEM
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class ActionStatus {
    PENDING, APPROVED, EXECUTED, CANCELLED, FAILED
}

data class ActionPlan(
    val actionId: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val parameters: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    var status: ActionStatus = ActionStatus.PENDING,
    var resultMessage: String? = null
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("actionId", actionId)
        obj.put("title", title)
        obj.put("description", description)
        obj.put("riskLevel", riskLevel.name)
        obj.put("requiresConfirmation", requiresConfirmation)
        obj.put("status", status.name)
        obj.put("resultMessage", resultMessage ?: "")
        val paramsObj = JSONObject()
        parameters.forEach { (k, v) -> paramsObj.put(k, v) }
        obj.put("parameters", paramsObj)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): ActionPlan? {
            return try {
                val obj = JSONObject(jsonStr)
                val params = mutableMapOf<String, String>()
                val paramsObj = obj.optJSONObject("parameters")
                if (paramsObj != null) {
                    val keys = paramsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        params[key] = paramsObj.optString(key, "")
                    }
                }
                ActionPlan(
                    actionId = obj.optString("actionId"),
                    title = obj.optString("title"),
                    description = obj.optString("description"),
                    riskLevel = try { RiskLevel.valueOf(obj.optString("riskLevel", "LOW")) } catch (_: Exception) { RiskLevel.LOW },
                    parameters = params,
                    requiresConfirmation = obj.optBoolean("requiresConfirmation", false),
                    status = try { ActionStatus.valueOf(obj.optString("status", "PENDING")) } catch (_: Exception) { ActionStatus.PENDING },
                    resultMessage = obj.optString("resultMessage").takeIf { it.isNotBlank() }
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val actionPlanJson: String? = null
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val timestamp: Long,
    val isCompleted: Boolean = false,
    val isAlarm: Boolean = false
)

@Entity(tableName = "workflows")
data class WorkflowItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerPhrase: String,
    val description: String,
    val stepsJson: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val keyName: String,
    val valueStr: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationSummaryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isImportant: Boolean = false
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean = false
)

enum class JarvisAssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING
}
