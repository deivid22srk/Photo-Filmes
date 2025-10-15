package com.telegram.videoorganizer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
}
