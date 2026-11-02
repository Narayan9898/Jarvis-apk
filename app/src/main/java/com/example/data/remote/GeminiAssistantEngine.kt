package com.example.data.remote

import android.content.Context
import com.example.BuildConfig
import com.example.data.model.ActionPlan
import com.example.data.model.ActionStatus
import com.example.data.model.RiskLevel
import com.example.domain.ActionRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class JarvisAiResponse(
    val spokenText: String,
    val plannedAction: ActionPlan? = null,
    val detectedLanguage: String = "en",
    val isOfflineFallback: Boolean = false
)

class GeminiAssistantEngine(
    private val context: Context
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val systemInstruction = """
        You are JARVIS (Just A Rather Very Intelligent System), an advanced personal AI assistant on an Android mobile device.
        Your tone is polite, witty, sophisticated, respectful, and highly competent.
        You support English, Telugu, Hindi, and Tanglish (Telugu-English blend).
        Always respond to the user in the language they used or are most comfortable with.
        
        When the user requests an action on their device or the web, formulate a structured action proposal using one of the allowed actions:
        - open_app: parameters {"app_name": "...", "package_name": "..."}
        - search_web: parameters {"query": "..."}
        - open_maps: parameters {"destination": "..."}
        - open_youtube: parameters {"query": "..."}
        - dial_phone: parameters {"target": "..."}
        - send_sms: parameters {"recipient": "...", "message": "..."}
        - create_reminder: parameters {"title": "...", "minutes_from_now": "..."}
        - set_alarm: parameters {"hour": "...", "minute": "...", "message": "..."}
        - set_timer: parameters {"seconds": "...", "message": "..."}
        - open_settings: parameters {"setting_type": "wifi|bluetooth|battery|display|accessibility|sound|apps"}
        - open_camera: parameters {}
        - open_whatsapp: parameters {"phone_number": "...", "text": "..."}
        - save_memory: parameters {"key": "...", "value": "..."}
        - summarize_notifications: parameters {}

        IMPORTANT:
        Respond STRICTLY in JSON format with two keys:
        {
          "response": "<Your conversational spoken response to the user>",
          "action": {
             "actionId": "<actionId or null>",
             "parameters": { ... }
          }
        }
        If no device action is needed (conversational query or question answering), set "action": null.
    """.trimIndent()

    suspend fun processQuery(
        userInput: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        apiKeyOverride: String = "",
        backendProxyUrl: String = "",
        preferredLanguage: String = "auto"
    ): JarvisAiResponse = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()

        // Check if offline fallback is preferred or forced
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            (field.get(null) as? String)?.takeIf { it != "MY_GEMINI_API_KEY" } ?: ""
        } catch (_: Throwable) {
            ""
        }

        val effectiveApiKey = when {
            apiKeyOverride.isNotBlank() -> apiKeyOverride
            buildConfigKey.isNotBlank() -> buildConfigKey
            else -> ""
        }

        if (effectiveApiKey.isBlank() && backendProxyUrl.isBlank()) {
            return@withContext ruleBasedNluFallback(trimmed, preferredLanguage)
        }

        // Attempt online LLM inference
        try {
            if (backendProxyUrl.isNotBlank()) {
                return@withContext queryBackendProxy(trimmed, backendProxyUrl, conversationHistory)
            } else {
                return@withContext queryDirectGemini(trimmed, effectiveApiKey, conversationHistory, preferredLanguage)
            }
        } catch (e: Exception) {
            // Graceful fallback to rule-based engine on network/quota error
            val fallback = ruleBasedNluFallback(trimmed, preferredLanguage)
            return@withContext fallback.copy(
                spokenText = fallback.spokenText + " (Offline mode: ${e.localizedMessage ?: "network unavailable"})",
                isOfflineFallback = true
            )
        }
    }

    private fun queryDirectGemini(
        userInput: String,
        apiKey: String,
        history: List<Pair<String, String>>,
        preferredLanguage: String
    ): JarvisAiResponse {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Append recent history (up to last 4 turns)
        history.takeLast(4).forEach { (role, msg) ->
            val partObj = JSONObject().put("text", msg)
            val partsArr = JSONArray().put(partObj)
            contentsArray.put(JSONObject().put("role", if (role == "user") "user" else "model").put("parts", partsArr))
        }

        // Append current prompt
        val userPromptWithLang = if (preferredLanguage != "auto") {
            "[Preferred language: $preferredLanguage] $userInput"
        } else userInput

        contentsArray.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userPromptWithLang)))
        )

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsArray)
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                }
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw IllegalStateException("Gemini API error ${response.code}: $errBody")
        }

        val responseString = response.body?.string() ?: "{}"
        return parseGeminiJsonResponse(responseString, userInput)
    }

    private fun queryBackendProxy(
        userInput: String,
        proxyUrl: String,
        history: List<Pair<String, String>>
    ): JarvisAiResponse {
        val reqJson = JSONObject().apply {
            put("query", userInput)
            val histArr = JSONArray()
            history.takeLast(4).forEach { (role, msg) ->
                histArr.put(JSONObject().put("role", role).put("text", msg))
            }
            put("history", histArr)
        }

        val request = Request.Builder()
            .url(proxyUrl)
            .post(reqJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Backend proxy error: ${response.code}")
        }

        val responseStr = response.body?.string() ?: "{}"
        return parseGeminiJsonResponse(responseStr, userInput)
    }

    private fun parseGeminiJsonResponse(jsonRaw: String, originalInput: String): JarvisAiResponse {
        val root = JSONObject(jsonRaw)
        val textContent = try {
            val candidates = root.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (_: Exception) {
            root.optString("response", "Processing request.")
        }

        var spokenText = "Understood."
        var actionPlan: ActionPlan? = null

        try {
            val parsed = JSONObject(textContent)
            spokenText = parsed.optString("response", "Understood, Sir.")
            val actionObj = parsed.optJSONObject("action")
            if (actionObj != null && !actionObj.isNull("actionId")) {
                val actionId = actionObj.getString("actionId")
                val paramsObj = actionObj.optJSONObject("parameters")
                val params = mutableMapOf<String, String>()
                if (paramsObj != null) {
                    val keys = paramsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        params[k] = paramsObj.optString(k, "")
                    }
                }

                val registered = ActionRegistry.getAction(actionId)
                if (registered != null) {
                    actionPlan = ActionPlan(
                        actionId = actionId,
                        title = registered.name,
                        description = registered.description,
                        riskLevel = registered.riskLevel,
                        parameters = params,
                        requiresConfirmation = registered.riskLevel != RiskLevel.LOW,
                        status = ActionStatus.PENDING
                    )
                }
            }
        } catch (_: Exception) {
            spokenText = textContent
        }

        val detectedLang = detectLanguage(originalInput)
        return JarvisAiResponse(
            spokenText = spokenText,
            plannedAction = actionPlan,
            detectedLanguage = detectedLang,
            isOfflineFallback = false
        )
    }

    fun ruleBasedNluFallback(userInput: String, preferredLanguage: String): JarvisAiResponse {
        val lower = userInput.lowercase().trim()
        val detectedLang = detectLanguage(userInput)

        // 1. YouTube
        if (lower.contains("youtube") || lower.contains("యూట్యూబ్")) {
            val query = lower.replace("open youtube", "").replace("youtube", "").replace("play", "").replace("ఓపెన్ చేయి", "").trim()
            val plan = ActionPlan(
                actionId = "open_youtube",
                title = "Open YouTube",
                description = "Open YouTube and play video",
                riskLevel = RiskLevel.LOW,
                parameters = if (query.isNotBlank()) mapOf("query" to query) else emptyMap()
            )
            val msg = when (detectedLang) {
                "te" -> "ఖచ్చితంగా, YouTube ఓపెన్ చేస్తున్నాను."
                "hi" -> "ज़रूर, YouTube खोला जा रहा है।"
                else -> "Right away, Sir. Opening YouTube."
            }
            return JarvisAiResponse(msg, plan, detectedLang, isOfflineFallback = true)
        }

        // 2. Open App generic
        if (lower.startsWith("open ") || lower.contains("ఓపెన్ చేయి") || lower.contains("kholo")) {
            val appQuery = lower.replace("open ", "")
                .replace("ఓపెన్ చేయి", "")
                .replace("kholo", "")
                .replace("app", "")
                .trim()
            val plan = ActionPlan(
                actionId = "open_app",
                title = "Open Application",
                description = "Launch $appQuery",
                riskLevel = RiskLevel.LOW,
                parameters = mapOf("app_name" to appQuery)
            )
            val msg = when (detectedLang) {
                "te" -> "$appQuery అప్లికేషన్ ఓపెన్ చేస్తున్నాను."
                "hi" -> "$appQuery खोला जा रहा है।"
                else -> "Launching $appQuery, Sir."
            }
            return JarvisAiResponse(msg, plan, detectedLang, isOfflineFallback = true)
        }

        // 3. Alarm
        if (lower.contains("alarm") || lower.contains("అలారం") || lower.contains("अलार्म")) {
            // Regex for time e.g. 6 am, 6:30, 7
            val hourRegex = Regex("(\\d{1,2})(:(\\d{2}))?\\s*(am|pm)?")
            val match = hourRegex.find(lower)
            var hour = 7
            var minute = 0
            if (match != null) {
                hour = match.groupValues[1].toIntOrNull() ?: 7
                minute = match.groupValues[3].toIntOrNull() ?: 0
                val ampm = match.groupValues[4]
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
            }
            val plan = ActionPlan(
                actionId = "set_alarm",
                title = "Set Alarm",
                description = "Set alarm for $hour:${"%02d".format(minute)}",
                riskLevel = RiskLevel.MEDIUM,
                parameters = mapOf(
                    "hour" to hour.toString(),
                    "minute" to minute.toString(),
                    "message" to "JARVIS Alarm"
                ),
                requiresConfirmation = true
            )
            val msg = when (detectedLang) {
                "te" -> "ఉదయం $hour:${"%02d".format(minute)} గంటలకు అలారం సెట్ చేయాలా?"
                "hi" -> "क्या मैं $hour:${"%02d".format(minute)} बजे का अलार्म सेट करूँ?"
                else -> "Shall I set an alarm for %02d:%02d, Sir?".format(hour, minute)
            }
            return JarvisAiResponse(msg, plan, detectedLang, isOfflineFallback = true)
        }

        // 4. Reminder
        if (lower.contains("remind") || lower.contains("గుర్తు చేయి") || lower.contains("याद")) {
            val title = userInput.replace(Regex("(?i)remind me to|remind me|గుర్తు చేయి|याद दिलाओ"), "").trim()
            val plan = ActionPlan(
                actionId = "create_reminder",
                title = "Create Reminder",
                description = "Reminder for: $title",
                riskLevel = RiskLevel.MEDIUM,
                parameters = mapOf("title" to if (title.isNotBlank()) title else "Scheduled Task"),
                requiresConfirmation = true
            )
            val msg = when (detectedLang) {
                "te" -> "'$title' కోసం రిమైండర్ సేవ్ చేయమంటారా?"
                "hi" -> "क्या मैं '$title' का रिमाइंडर जोड़ूँ?"
                else -> "Would you like me to schedule a reminder for '$title'?"
            }
            return JarvisAiResponse(msg, plan, detectedLang, isOfflineFallback = true)
        }

        // 5. Dial phone
        if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.contains("ఫోన్ చేయి")) {
            val target = lower.replace("call ", "").replace("dial ", "").replace("ఫోన్ చేయి", "").trim()
            val plan = ActionPlan(
                actionId = "dial_phone",
                title = "Dial Phone",
                description = "Prepare call to $target",
                riskLevel = RiskLevel.MEDIUM,
                parameters = mapOf("target" to target),
                requiresConfirmation = true
            )
            val msg = when (detectedLang) {
                "te" -> "$target కి కాల్ చేయడానికి డయలర్ ఓపెన్ చేయాలా?"
                "hi" -> "क्या $target को कॉल मिलाएँ?"
                else -> "I have prepared the dialer for $target. Please confirm to proceed."
            }
            return JarvisAiResponse(msg, plan, detectedLang, isOfflineFallback = true)
        }

        // 6. Settings
        if (lower.contains("settings") || lower.contains("సెట్టింగ్స్") || lower.contains("wifi") || lower.contains("bluetooth")) {
            val settingType = when {
                lower.contains("wifi") -> "wifi"
                lower.contains("bluetooth") -> "bluetooth"
                lower.contains("battery") -> "battery"
                lower.contains("accessibility") -> "accessibility"
                else -> "general"
            }
            val plan = ActionPlan(
                actionId = "open_settings",
                title = "Open System Settings",
                description = "Open $settingType settings",
                riskLevel = RiskLevel.LOW,
                parameters = mapOf("setting_type" to settingType)
            )
            return JarvisAiResponse("Opening $settingType settings.", plan, detectedLang, isOfflineFallback = true)
        }

        // 7. Web search
        if (lower.startsWith("search ") || lower.startsWith("google ") || lower.contains("వెతుకు")) {
            val query = lower.replace("search ", "").replace("google ", "").replace("వెతుకు", "").trim()
            val plan = ActionPlan(
                actionId = "search_web",
                title = "Search Internet",
                description = "Search for $query",
                riskLevel = RiskLevel.LOW,
                parameters = mapOf("query" to query)
            )
            return JarvisAiResponse("Searching Google for $query.", plan, detectedLang, isOfflineFallback = true)
        }

        // Default conversational greeting/response
        val response = when (detectedLang) {
            "te" -> "నమస్కారం! నేను JARVIS. మీ ఆదేశం కోసం ఎదురు చూస్తున్నాను. YouTube, అలారాలు, లేదా కాల్స్ కోసం చెప్పండి."
            "hi" -> "नमस्ते! मैं JARVIS हूँ। मैं आपके आदेश की प्रतीक्षा कर रहा हूँ।"
            else -> "At your service, Sir. All systems fully operational. How may I assist you today?"
        }
        return JarvisAiResponse(response, null, detectedLang, isOfflineFallback = true)
    }

    private fun detectLanguage(text: String): String {
        return when {
            text.any { it in '\u0C00'..'\u0C7F' } -> "te" // Telugu unicode block
            text.any { it in '\u0900'..'\u097F' } -> "hi" // Devanagari unicode block
            else -> "en"
        }
    }
}
