package com.telegram.videoorganizer.data.preferences

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val BOT_TOKEN_KEY = stringPreferencesKey("bot_token")
        private val LOGGING_ENABLED_KEY = booleanPreferencesKey("logging_enabled")
    }
    
    val botToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[BOT_TOKEN_KEY]
        }
    
    val loggingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LOGGING_ENABLED_KEY] ?: false
        }
    
    suspend fun saveBotToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[BOT_TOKEN_KEY] = token
        }
    }
    
    suspend fun clearBotToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(BOT_TOKEN_KEY)
        }
    }
    
    suspend fun setLoggingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOGGING_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun exportSettings(): Result<String> {
        return try {
            val preferences = context.dataStore.data.first()
            val settingsJson = JSONObject().apply {
                preferences[BOT_TOKEN_KEY]?.let { put("bot_token", it) }
                put("logging_enabled", preferences[LOGGING_ENABLED_KEY] ?: false)
            }
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val internalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val targetDir = if (downloadsDir?.exists() == true && downloadsDir.canWrite()) {
                downloadsDir
            } else {
                internalDir ?: context.filesDir
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "video_organizer_settings_$timestamp.json"
            val file = File(targetDir, fileName)
            
            file.writeText(settingsJson.toString(2))
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importSettings(filePath: String): Result<Boolean> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(Exception("File not found"))
            }
            
            val jsonString = file.readText()
            val settingsJson = JSONObject(jsonString)
            
            context.dataStore.edit { preferences ->
                if (settingsJson.has("bot_token")) {
                    preferences[BOT_TOKEN_KEY] = settingsJson.getString("bot_token")
                }
                if (settingsJson.has("logging_enabled")) {
                    preferences[LOGGING_ENABLED_KEY] = settingsJson.getBoolean("logging_enabled")
                }
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllSettings(): Map<String, Any?> {
        val preferences = context.dataStore.data.first()
        return mapOf(
            "bot_token" to preferences[BOT_TOKEN_KEY],
            "logging_enabled" to (preferences[LOGGING_ENABLED_KEY] ?: false)
        )
    }
}
