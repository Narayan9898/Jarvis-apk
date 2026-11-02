package com.example

import android.app.Application
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisPreferences
import com.example.data.remote.GeminiAssistantEngine
import com.example.domain.ActionExecutor

class JarvisApplication : Application() {

    lateinit var database: JarvisDatabase
        private set

    lateinit var preferences: JarvisPreferences
        private set

    lateinit var actionExecutor: ActionExecutor
        private set

    lateinit var aiEngine: GeminiAssistantEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getDatabase(this)
        preferences = JarvisPreferences(this)
        actionExecutor = ActionExecutor(this, database)
        aiEngine = GeminiAssistantEngine(this)
    }
}
