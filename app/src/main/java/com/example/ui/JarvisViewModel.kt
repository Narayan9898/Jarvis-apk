package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApplication
import com.example.data.local.JarvisPreferences
import com.example.data.model.ActionPlan
import com.example.data.model.ActionStatus
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisAssistantState
import com.example.data.model.MemoryItem
import com.example.data.model.MessageSender
import com.example.data.model.ReminderItem
import com.example.data.model.WorkflowItem
import com.example.domain.ActionRegistry
import com.example.domain.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApplication
    private val db = app.database
    private val prefs = app.preferences
    private val executor = app.actionExecutor
    private val aiEngine = app.aiEngine

    private val _assistantState = MutableStateFlow(JarvisAssistantState.IDLE)
    val assistantState: StateFlow<JarvisAssistantState> = _assistantState.asStateFlow()

    private val _activeActionPlan = MutableStateFlow<ActionPlan?>(null)
    val activeActionPlan: StateFlow<ActionPlan?> = _activeActionPlan.asStateFlow()

    private val _statusBannerText = MutableStateFlow<String?>(null)
    val statusBannerText: StateFlow<String?> = _statusBannerText.asStateFlow()

    private val _audioWaveform = MutableStateFlow(0f)
    val audioWaveform: StateFlow<Float> = _audioWaveform.asStateFlow()

    val chatMessages = db.chatDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reminders = db.reminderDao().getAllReminders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val workflows = db.workflowDao().getAllWorkflows()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val memories = db.memoryDao().getAllMemories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notifications = db.notificationDao().getRecentNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Preferences flows
    val languagePref = prefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "auto")
    val voiceFeedbackPref = prefs.voiceFeedbackFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val offlineModePref = prefs.offlineModeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val geminiApiKeyPref = prefs.geminiApiKeyFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")
    val backendProxyUrlPref = prefs.backendProxyUrlFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    private val voiceManager = VoiceManager(
        context = application.applicationContext,
        onSpeechRecognized = { text, isFinal ->
            if (isFinal) {
                processUserPrompt(text)
            }
        },
        onError = { err ->
            _assistantState.value = JarvisAssistantState.IDLE
            _statusBannerText.value = err
        }
    )

    init {
        // Collect voice states
        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    _assistantState.value = JarvisAssistantState.LISTENING
                } else if (_assistantState.value == JarvisAssistantState.LISTENING) {
                    _assistantState.value = JarvisAssistantState.IDLE
                }
            }
        }
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _assistantState.value = JarvisAssistantState.SPEAKING
                } else if (_assistantState.value == JarvisAssistantState.SPEAKING) {
                    _assistantState.value = JarvisAssistantState.IDLE
                }
            }
        }
        viewModelScope.launch {
            voiceManager.audioRms.collect { rms ->
                _audioWaveform.value = rms
            }
        }

        // Initialize default sample workflows if database is empty
        viewModelScope.launch {
            val existing = db.workflowDao().getEnabledWorkflows()
            if (existing.isEmpty()) {
                initDefaultWorkflows()
            }
        }
    }

    private suspend fun initDefaultWorkflows() {
        val morningWorkflow = WorkflowItem(
            title = "Good Morning Routine",
            triggerPhrase = "Good morning JARVIS",
            description = "Opens weather, checks notifications, and presents today's schedule.",
            stepsJson = """[{"actionId":"search_web","parameters":{"query":"today weather forecast"}},{"actionId":"summarize_notifications","parameters":{}}]"""
        )
        val sleepWorkflow = WorkflowItem(
            title = "Bedtime Protocol",
            triggerPhrase = "I am going to sleep",
            description = "Sets tomorrow morning's alarm and launches clock.",
            stepsJson = """[{"actionId":"set_alarm","parameters":{"hour":"7","minute":"0","message":"Morning Alarm"}}]"""
        )
        db.workflowDao().insertWorkflow(morningWorkflow)
        db.workflowDao().insertWorkflow(sleepWorkflow)
    }

    fun startListening() {
        val currentLang = languagePref.value
        voiceManager.startListening(currentLang)
    }

    fun stopListening() {
        voiceManager.stopListening()
        _assistantState.value = JarvisAssistantState.IDLE
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _assistantState.value = JarvisAssistantState.IDLE
    }

    fun sendTextCommand(text: String) {
        if (text.isNotBlank()) {
            processUserPrompt(text.trim())
        }
    }

    private fun processUserPrompt(userInput: String) {
        viewModelScope.launch {
            // Save user message to chat DB
            val userMsg = ChatMessage(
                text = userInput,
                sender = MessageSender.USER,
                timestamp = System.currentTimeMillis()
            )
            db.chatDao().insertMessage(userMsg)

            _assistantState.value = JarvisAssistantState.THINKING
            _statusBannerText.value = "Analyzing request..."

            // Check if this input matches an active custom workflow trigger
            val allWorkflows = workflows.value
            val matchingWorkflow = allWorkflows.find {
                it.isEnabled && userInput.contains(it.triggerPhrase, ignoreCase = true)
            }
            if (matchingWorkflow != null) {
                runWorkflow(matchingWorkflow)
                return@launch
            }

            // Gather recent conversation history for context
            val history = chatMessages.value.takeLast(6).map {
                (if (it.sender == MessageSender.USER) "user" else "model") to it.text
            }

            val lang = languagePref.value
            val apiKey = geminiApiKeyPref.value
            val proxyUrl = backendProxyUrlPref.value
            val offlineForced = offlineModePref.value

            val response = if (offlineForced) {
                aiEngine.ruleBasedNluFallback(userInput, lang)
            } else {
                aiEngine.processQuery(
                    userInput = userInput,
                    conversationHistory = history,
                    apiKeyOverride = apiKey,
                    backendProxyUrl = proxyUrl,
                    preferredLanguage = lang
                )
            }

            // Record assistant response
            val assistantMsg = ChatMessage(
                text = response.spokenText,
                sender = MessageSender.JARVIS,
                language = response.detectedLanguage,
                actionPlanJson = response.plannedAction?.toJson()
            )
            db.chatDao().insertMessage(assistantMsg)

            // Speak the response if voice feedback is enabled
            if (voiceFeedbackPref.value) {
                voiceManager.speak(response.spokenText, response.detectedLanguage)
            }

            // Handle proposed action
            val action = response.plannedAction
            if (action != null) {
                if (action.requiresConfirmation) {
                    _activeActionPlan.value = action
                    _assistantState.value = JarvisAssistantState.IDLE
                    _statusBannerText.value = "Awaiting your confirmation for: ${action.title}"
                } else {
                    executePlannedAction(action)
                }
            } else {
                _assistantState.value = JarvisAssistantState.IDLE
                _statusBannerText.value = null
            }
        }
    }

    fun confirmActiveAction() {
        val plan = _activeActionPlan.value ?: return
        _activeActionPlan.value = null
        executePlannedAction(plan.copy(status = ActionStatus.APPROVED))
    }

    fun cancelActiveAction() {
        val plan = _activeActionPlan.value ?: return
        _activeActionPlan.value = null
        _assistantState.value = JarvisAssistantState.IDLE
        _statusBannerText.value = "Action cancelled."
        viewModelScope.launch {
            val cancelledMsg = ChatMessage(
                text = "Cancelled action: ${plan.title}.",
                sender = MessageSender.SYSTEM,
                actionPlanJson = plan.copy(status = ActionStatus.CANCELLED).toJson()
            )
            db.chatDao().insertMessage(cancelledMsg)
        }
    }

    private fun executePlannedAction(plan: ActionPlan) {
        viewModelScope.launch {
            _assistantState.value = JarvisAssistantState.EXECUTING
            _statusBannerText.value = "Executing: ${plan.title}..."

            val result = executor.execute(plan)

            _assistantState.value = JarvisAssistantState.IDLE
            _statusBannerText.value = result.message

            // Insert audit confirmation in chat
            val resultMsg = ChatMessage(
                text = result.message,
                sender = MessageSender.SYSTEM,
                actionPlanJson = result.actionPlan.toJson()
            )
            db.chatDao().insertMessage(resultMsg)

            if (voiceFeedbackPref.value && result.success) {
                voiceManager.speak(result.message)
            }
        }
    }

    fun runWorkflow(workflow: WorkflowItem) {
        viewModelScope.launch {
            _statusBannerText.value = "Running workflow: ${workflow.title}"
            val startMsg = ChatMessage(
                text = "Executing automated workflow: ${workflow.title}",
                sender = MessageSender.JARVIS
            )
            db.chatDao().insertMessage(startMsg)

            try {
                val array = org.json.JSONArray(workflow.stepsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val actionId = obj.getString("actionId")
                    val paramsObj = obj.optJSONObject("parameters")
                    val params = mutableMapOf<String, String>()
                    if (paramsObj != null) {
                        val keys = paramsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            params[k] = paramsObj.getString(k)
                        }
                    }
                    val plan = ActionPlan(
                        actionId = actionId,
                        title = ActionRegistry.getAction(actionId)?.name ?: actionId,
                        description = "Automated workflow step",
                        parameters = params,
                        requiresConfirmation = false
                    )
                    executor.execute(plan)
                    kotlinx.coroutines.delay(800)
                }
                _statusBannerText.value = "Workflow completed: ${workflow.title}"
            } catch (e: Exception) {
                _statusBannerText.value = "Workflow interrupted: ${e.localizedMessage}"
            }
        }
    }

    fun toggleWorkflow(workflow: WorkflowItem) {
        viewModelScope.launch {
            db.workflowDao().updateWorkflow(workflow.copy(isEnabled = !workflow.isEnabled))
        }
    }

    fun deleteWorkflow(workflow: WorkflowItem) {
        viewModelScope.launch {
            db.workflowDao().deleteWorkflow(workflow)
        }
    }

    fun addWorkflow(title: String, triggerPhrase: String, description: String, stepsJson: String) {
        viewModelScope.launch {
            val item = WorkflowItem(
                title = title,
                triggerPhrase = triggerPhrase,
                description = description,
                stepsJson = stepsJson
            )
            db.workflowDao().insertWorkflow(item)
        }
    }

    fun addReminder(title: String, minutesFromNow: Long, isAlarm: Boolean) {
        viewModelScope.launch {
            val plan = ActionPlan(
                actionId = if (isAlarm) "set_alarm" else "create_reminder",
                title = if (isAlarm) "Set Alarm" else "Create Reminder",
                description = title,
                parameters = mapOf(
                    "title" to title,
                    "minutes_from_now" to minutesFromNow.toString()
                )
            )
            executor.execute(plan)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            db.reminderDao().deleteReminder(reminder)
        }
    }

    fun toggleReminderCompleted(reminder: ReminderItem) {
        viewModelScope.launch {
            db.reminderDao().updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.chatDao().clearHistory()
            _statusBannerText.value = "Conversation history cleared."
        }
    }

    fun deleteMemory(item: MemoryItem) {
        viewModelScope.launch {
            db.memoryDao().deleteMemory(item)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            db.memoryDao().clearAllMemories()
        }
    }

    fun saveMemory(key: String, value: String) {
        viewModelScope.launch {
            db.memoryDao().insertMemory(
                MemoryItem(
                    category = "PREFERENCE",
                    keyName = key,
                    valueStr = value
                )
            )
        }
    }

    // Preferences setters
    fun setLanguage(lang: String) = viewModelScope.launch { prefs.setLanguage(lang) }
    fun setSpeechRate(rate: Float) = viewModelScope.launch { prefs.setSpeechRate(rate) }
    fun setVoiceFeedback(enabled: Boolean) = viewModelScope.launch { prefs.setVoiceFeedback(enabled) }
    fun setOfflineMode(enabled: Boolean) = viewModelScope.launch { prefs.setOfflineMode(enabled) }
    fun setGeminiApiKey(key: String) = viewModelScope.launch { prefs.setGeminiApiKey(key) }
    fun setBackendProxyUrl(url: String) = viewModelScope.launch { prefs.setBackendProxyUrl(url) }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
