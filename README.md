# JARVIS AI — Advanced Personal Android Assistant

[![Build & Release Debug APK](https://github.com/aistudio/jarvis-ai-assistant/actions/workflows/build-apk.yml/badge.svg)](https://github.com/aistudio/jarvis-ai-assistant/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![AI Engine](https://img.shields.io/badge/Gemini_API-2.5_Flash-orange.svg)](https://ai.google.dev)

A complete, production-oriented Android AI personal assistant inspired by fictional **J.A.R.V.I.S.** Engineered with Clean Architecture, MVVM, Jetpack Compose Material 3, Android Room SQLite persistence, DataStore preferences, speech recognition, multi-lingual NLU (Telugu, Hindi, English, and Tanglish), and a controlled safe Action Registry.

---

## 🌟 Key Features

### 1. Futuristic Sci-Fi Interface
- **Animated Arc Reactor AI Core:** Multi-layered concentric mechanical rings with counter-rotations, ambient glowing gradients, and audio RMS reactive pulses.
- **Dynamic Waveform Visualizer:** Real-time audio waveform responding to listening and vocalization states.
- **State Feedback:** Distinct visual HUD states for `LISTENING`, `THINKING`, `SPEAKING`, `EXECUTING`, and `IDLE`.
- **Cyberpunk Dark Theme:** Deep Navy, Electric Blue, and Cyan palette adhering to Material Design 3 guidelines.

### 2. Multi-Lingual Voice Assistant Engine
- **Supported Languages:** Native support for English (`en-IN`, `en-US`), Telugu (`te-IN`), Hindi (`hi-IN`), and Tanglish (Telugu-English blend).
- **Voice Pipeline:**
  1. Push-to-talk microphone activation with runtime `RECORD_AUDIO` permissions.
  2. Android `SpeechRecognizer` streaming audio and extracting partial/final transcripts.
  3. Script/language detection (Telugu unicode `\u0C00-\u0C7F`, Devanagari `\u0900-\u097F`).
  4. AI planning via Google Gemini API or local rule-based NLU fallback.
  5. Action validation against the strictly sandboxed Action Registry.
  6. Multi-lingual vocalization through Android `TextToSpeech`.

### 3. Sandboxed Action Registry & Safety Engine
The AI model is never allowed to execute arbitrary scripts, shell commands, or dynamic code. Every action is registered in a strictly validated catalog with defined parameters and risk tiers:
- **Low Risk:**
  - `open_app`: Queries PackageManager and launches installed Android apps.
  - `search_web`: Web search via browser view intents.
  - `open_maps`: Navigation and directions via Google Maps intent.
  - `open_youtube`: Launches YouTube video searches.
  - `open_settings`: Opens Wi-Fi, Bluetooth, Battery, Display, or Accessibility settings.
  - `open_camera`: Launches system camera.
  - `set_timer`: Sets countdown timers via Android AlarmClock.
- **Medium Risk (Requires Explicit User Confirmation):**
  - `dial_phone`: Opens system dialer with target contact or phone number.
  - `send_sms`: Composes SMS in messaging app with prefilled text.
  - `create_reminder`: Stores persistent reminders in Room and inserts system Calendar events.
  - `set_alarm`: Schedules exact system clock alarms.
  - `open_whatsapp`: Opens chat conversation with prefilled text.
  - `save_memory`: Stores user facts to the Cognitive Archive.
- **High Risk:**
  - Destructive file deletes and financial transactions are strictly blocked by safety policy.

### 4. Zero-Leakage Notification Assistant
- Optional `NotificationListenerService` capturing incoming alerts.
- **Privacy Policy:** Strictly excludes banking apps (e.g. SBI, HDFC, ICICI, Paytm, PhonePe, GPay), authenticators (Google Authenticator, Authy), and password managers.
- Automatically discards OTPs, verification codes, and security tokens locally.

### 5. Automation Protocols & Workflows
- Multi-step automation engine (e.g. "Good morning JARVIS", "Bedtime Protocol").
- Visual routine builder: add, edit, enable/disable, and execute automated sequential actions.

### 6. Cognitive Archives (Personal Memory)
- User-controlled memory system for preferred languages, favorite places, and custom preferences.
- Full transparency: users can inspect, add, or purge memories at any time.

---

## 🏗️ Architecture & Project Structure

The project follows **Clean Architecture & MVVM**:

```
app/src/main/java/com/example/
├── JarvisApplication.kt          # Global Application container initializing DB & services
├── MainActivity.kt               # Edge-to-edge Compose entrypoint, M3 Scaffold & navigation
├── data/
│   ├── model/
│   │   └── Models.kt             # ChatMessage, ActionPlan, Reminder, Workflow, Memory entities
│   ├── local/
│   │   ├── JarvisDatabase.kt     # Room database & DAOs (Chat, Reminder, Workflow, Memory, Notifications)
│   │   └── JarvisPreferences.kt  # DataStore for user preferences & settings
│   └── remote/
│       └── GeminiAssistantEngine.kt # Gemini 2.5 Flash API client & offline NLU fallback
├── domain/
│   ├── ActionRegistry.kt         # Allowlisted action registry, schemas & risk validation
│   ├── ActionExecutor.kt         # Safe Android Intent & Provider executors
│   └── VoiceManager.kt           # SpeechRecognizer & TextToSpeech controller
├── service/
│   └── JarvisNotificationListenerService.kt # Privacy-first notification listener
└── ui/
    ├── JarvisViewModel.kt        # StateFlow orchestration of voice, NLU, and actions
    ├── components/
    │   ├── ArcReactorVisualizer.kt       # Animated canvas Arc Reactor AI core
    │   ├── AudioWaveformVisualizer.kt    # Real-time audio waveform
    │   └── ActionConfirmationDialog.kt   # Risk-based authorization card
    ├── screens/
    │   ├── DashboardScreen.kt    # Futuristic HUD & voice control
    │   ├── ChatHistoryScreen.kt  # Transcripts & action audit receipts
    │   ├── InstalledAppsScreen.kt# Real PackageManager app launcher
    │   ├── RemindersScreen.kt    # Schedule, alarms & reminders
    │   ├── WorkflowsScreen.kt    # Routine automation builder
    │   ├── NotificationsScreen.kt# Summarized alerts
    │   ├── PermissionsScreen.kt  # Security dashboard & permission audit
    │   ├── MemoryScreen.kt       # Cognitive archive manager
    │   └── SettingsScreen.kt     # Gateway, speech velocity, language config
    └── theme/
        ├── Color.kt              # Cyber cyan, electric blue, deep navy palette
        ├── Theme.kt              # Futuristic Dark Material 3 theme
        └── Type.kt               # Typography definitions
```

---

## 🔒 Security & Privacy Policy

1. **No Hardcoded API Keys:** API keys are injected at build time via Secrets Gradle Plugin through `.env` or AI Studio Secrets.
2. **Production Proxy Gateway:** A complete Node.js/TypeScript backend proxy is included in `/backend/` with rate limiting, bearer authentication, and input size constraints.
3. **Least-Privilege Permissions:** Permissions (`RECORD_AUDIO`, `READ_CONTACTS`, `READ_CALENDAR`, `CAMERA`) are requested at runtime only when the corresponding feature is triggered.
4. **Offline Resilience:** If network is unavailable or no API key is configured, JARVIS switches seamlessly to its local rule-based intent engine so device actions (alarms, timers, apps, settings) continue to work offline.

---

## 📦 Automated APK Builds via GitHub Actions

This repository includes `.github/workflows/build-apk.yml`:
- **Automatic Build:** Every push to `main` builds the release/debug APK.
- **Direct Download:** Download the compiled APK directly from the GitHub Actions Artifacts tab (`jarvis-ai-debug-apk`).
- **GitHub Releases:** Tagging a commit (e.g. `v1.0.0`) automatically publishes a GitHub Release with the standalone `.apk` attached.

### Manual Local Build Instructions

```bash
# Build Debug APK
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk

# Run local Robolectric & unit tests
gradle :app:testDebugUnitTest
```

---

## 🌐 Production Backend Gateway Setup

To deploy the secure backend proxy:

```bash
cd backend
npm install
cp .env.example .env # Set your GEMINI_API_KEY and CLIENT_AUTH_TOKEN
npm start
```
Then in the JARVIS app settings, enter your backend URL (e.g., `https://your-server.com/api/chat`).
