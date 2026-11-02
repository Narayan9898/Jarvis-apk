package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

class JarvisPreferences(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("pref_language")
        val KEY_SPEECH_RATE = floatPreferencesKey("pref_speech_rate")
        val KEY_SPEECH_PITCH = floatPreferencesKey("pref_speech_pitch")
        val KEY_VOICE_FEEDBACK = booleanPreferencesKey("pref_voice_feedback")
        val KEY_OFFLINE_MODE = booleanPreferencesKey("pref_offline_mode")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("pref_gemini_api_key")
        val KEY_BACKEND_PROXY_URL = stringPreferencesKey("pref_backend_proxy_url")
        val KEY_USER_NAME = stringPreferencesKey("pref_user_name")
        val KEY_CONFIRM_SENSITIVE = booleanPreferencesKey("pref_confirm_sensitive")
        val KEY_NOTIFICATION_ASSISTANT = booleanPreferencesKey("pref_notification_assistant")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "auto" }
    val speechRateFlow: Flow<Float> = context.dataStore.data.map { it[KEY_SPEECH_RATE] ?: 1.0f }
    val speechPitchFlow: Flow<Float> = context.dataStore.data.map { it[KEY_SPEECH_PITCH] ?: 1.0f }
    val voiceFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_VOICE_FEEDBACK] ?: true }
    val offlineModeFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_OFFLINE_MODE] ?: false }
    val geminiApiKeyFlow: Flow<String> = context.dataStore.data.map { it[KEY_GEMINI_API_KEY] ?: "" }
    val backendProxyUrlFlow: Flow<String> = context.dataStore.data.map { it[KEY_BACKEND_PROXY_URL] ?: "" }
    val userNameFlow: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: "Sir" }
    val confirmSensitiveFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONFIRM_SENSITIVE] ?: true }
    val notificationAssistantFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATION_ASSISTANT] ?: true }

    suspend fun setLanguage(lang: String) = context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    suspend fun setSpeechRate(rate: Float) = context.dataStore.edit { it[KEY_SPEECH_RATE] = rate }
    suspend fun setSpeechPitch(pitch: Float) = context.dataStore.edit { it[KEY_SPEECH_PITCH] = pitch }
    suspend fun setVoiceFeedback(enabled: Boolean) = context.dataStore.edit { it[KEY_VOICE_FEEDBACK] = enabled }
    suspend fun setOfflineMode(enabled: Boolean) = context.dataStore.edit { it[KEY_OFFLINE_MODE] = enabled }
    suspend fun setGeminiApiKey(key: String) = context.dataStore.edit { it[KEY_GEMINI_API_KEY] = key }
    suspend fun setBackendProxyUrl(url: String) = context.dataStore.edit { it[KEY_BACKEND_PROXY_URL] = url }
    suspend fun setUserName(name: String) = context.dataStore.edit { it[KEY_USER_NAME] = name }
    suspend fun setConfirmSensitive(enabled: Boolean) = context.dataStore.edit { it[KEY_CONFIRM_SENSITIVE] = enabled }
    suspend fun setNotificationAssistant(enabled: Boolean) = context.dataStore.edit { it[KEY_NOTIFICATION_ASSISTANT] = enabled }
}
