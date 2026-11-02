package com.example.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (text: String, isFinal: Boolean) -> Unit,
    private val onError: (errorMsg: String) -> Unit
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun startListening(languageCode: String = "auto") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition service is not available on this device.")
            return
        }

        stopListening()
        stopSpeaking()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    _audioRms.value = (rmsdB + 2f).coerceIn(0f, 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioRms.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                        SpeechRecognizer.ERROR_NETWORK -> "Network issue while recognizing speech."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        SpeechRecognizer.ERROR_CLIENT -> "Voice client cancelled."
                        else -> "Speech recognition error code: $error"
                    }
                    if (error != SpeechRecognizer.ERROR_CLIENT && error != SpeechRecognizer.ERROR_NO_MATCH) {
                        onError(message)
                    }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onSpeechRecognized(text, true)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onSpeechRecognized(text, false)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            val localeTag = when (languageCode) {
                "te" -> "te-IN"
                "hi" -> "hi-IN"
                "en" -> "en-IN"
                else -> Locale.getDefault().toLanguageTag()
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("en-IN", "te-IN", "hi-IN"))
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun speak(
        text: String,
        languageCode: String = "auto",
        pitch: Float = 1.0f,
        speed: Float = 1.0f
    ) {
        if (!isTtsInitialized || textToSpeech == null) return

        stopSpeaking()

        val locale = when (languageCode) {
            "te" -> Locale("te", "IN")
            "hi" -> Locale("hi", "IN")
            "en" -> Locale.ENGLISH
            else -> {
                // Auto-detect script: Telugu script is \u0C00-\u0C7F, Devanagari is \u0900-\u097F
                when {
                    text.any { it in '\u0C00'..'\u0C7F' } -> Locale("te", "IN")
                    text.any { it in '\u0900'..'\u097F' } -> Locale("hi", "IN")
                    else -> Locale.ENGLISH
                }
            }
        }

        textToSpeech?.language = locale
        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(speed)

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            textToSpeech?.stop()
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsInitialized = false
    }
}
