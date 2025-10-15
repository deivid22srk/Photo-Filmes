package com.telegram.videoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telegram.videoorganizer.data.model.Series
import com.telegram.videoorganizer.data.preferences.PreferencesManager
import com.telegram.videoorganizer.data.repository.TelegramRepository
import com.telegram.videoorganizer.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = TelegramRepository()
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _isLoadingVideoUrl = MutableStateFlow(false)
    val isLoadingVideoUrl: StateFlow<Boolean> = _isLoadingVideoUrl.asStateFlow()
    
    private val _videoError = MutableStateFlow<String?>(null)
    val videoError: StateFlow<String?> = _videoError.asStateFlow()
    
    private val videoUrlCache = mutableMapOf<String, String>()
    
    init {
        loadBotToken()
        loadLoggingPreference()
    }
    
    private fun loadBotToken() {
        viewModelScope.launch {
            preferencesManager.botToken.collect { token ->
                _uiState.value = _uiState.value.copy(
                    botToken = token,
                    isConnected = token != null
                )
                if (token != null) {
                    verifyAndLoadVideos(token)
                }
            }
        }
    }
    
    fun saveBotToken(token: String) {
        viewModelScope.launch {
            Logger.i("MainViewModel", "Attempting to save bot token")
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            Logger.d("MainViewModel", "Token cleaned, length: ${cleanToken.length}")
            val result = repository.verifyToken(cleanToken)
            
            result.onSuccess {
                Logger.i("MainViewModel", "Bot token verified and saved successfully")
                preferencesManager.saveBotToken(cleanToken)
                _uiState.value = _uiState.value.copy(
                    botToken = cleanToken,
                    isConnected = true,
                    isLoading = false,
                    error = null
                )
                loadVideos(cleanToken)
            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to save bot token: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Connection failed"
                )
            }
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            preferencesManager.clearBotToken()
            _uiState.value = UiState()
        }
    }
    
    fun refreshVideos() {
        viewModelScope.launch {
            val token = preferencesManager.botToken.first()
            if (token != null) {
                loadVideos(token)
            }
        }
    }
    
    private fun verifyAndLoadVideos(token: String) {
        viewModelScope.launch {
            val result = repository.verifyToken(token)
            if (result.isSuccess) {
                loadVideos(token)
            }
        }
    }
    
    private fun loadVideos(token: String) {
        viewModelScope.launch {
            Logger.i("MainViewModel", "Loading videos from Telegram")
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getUpdates(token)
            
            result.onSuccess { updates ->
                Logger.i("MainViewModel", "Received ${updates.size} updates")
                val series = repository.parseVideosToSeries(updates)
                Logger.i("MainViewModel", "Loaded ${series.size} series")
                _uiState.value = _uiState.value.copy(
                    series = series,
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to load videos: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load videos"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadLoggingPreference() {
        viewModelScope.launch {
            preferencesManager.loggingEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(loggingEnabled = enabled)
                Logger.setEnabled(enabled)
            }
        }
    }
    
    fun setLoggingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLoggingEnabled(enabled)
            Logger.setEnabled(enabled)
            Logger.i("MainViewModel", "Logging ${if (enabled) "enabled" else "disabled"}")
        }
    }
    
    fun getLogFilePath(): String? {
        return Logger.getLogFilePath()
    }
    
    fun clearLogs() {
        Logger.clearLogs()
    }
    
    fun getLogSize(): Long {
        return Logger.getLogSize()
    }
    
    fun getVideoUrl(fileId: String): kotlinx.coroutines.flow.Flow<String?> = kotlinx.coroutines.flow.flow {
        if (videoUrlCache.containsKey(fileId)) {
            emit(videoUrlCache[fileId])
            return@flow
        }
        
        _isLoadingVideoUrl.value = true
        _videoError.value = null
        
        val token = preferencesManager.botToken.first()
        if (token == null) {
            _videoError.value = "Bot token not found"
            _isLoadingVideoUrl.value = false
            emit(null)
            return@flow
        }
        
        try {
            Logger.i("MainViewModel", "Getting file info for fileId: $fileId")
            val fileResult = repository.getFile(token, fileId)
            
            fileResult.onSuccess { file ->
                val videoUrl = "https://api.telegram.org/file/bot$token/${file.filePath}"
                Logger.i("MainViewModel", "Video URL obtained: $videoUrl")
                videoUrlCache[fileId] = videoUrl
                _isLoadingVideoUrl.value = false
                emit(videoUrl)
            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to get video URL: ${error.message}")
                _videoError.value = error.message ?: "Failed to load video"
                _isLoadingVideoUrl.value = false
                emit(null)
            }
        } catch (e: Exception) {
            Logger.e("MainViewModel", "Exception getting video URL: ${e.message}")
            _videoError.value = e.message ?: "Failed to load video"
            _isLoadingVideoUrl.value = false
            emit(null)
        }
    }
    
    fun retryLoadVideo(fileId: String) {
        videoUrlCache.remove(fileId)
        _videoError.value = null
    }
    
    fun exportSettings(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            Logger.i("MainViewModel", "Exporting settings...")
            val result = preferencesManager.exportSettings()
            result.onSuccess { path ->
                Logger.i("MainViewModel", "Settings exported to: $path")
            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to export settings: ${error.message}")
            }
            onResult(result)
        }
    }
    
    fun importSettings(filePath: String, onResult: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            Logger.i("MainViewModel", "Importing settings from: $filePath")
            val result = preferencesManager.importSettings(filePath)
            result.onSuccess {
                Logger.i("MainViewModel", "Settings imported successfully")
            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to import settings: ${error.message}")
            }
            onResult(result)
        }
    }
    
    data class UiState(
        val botToken: String? = null,
        val isConnected: Boolean = false,
        val isLoading: Boolean = false,
        val series: List<Series> = emptyList(),
        val error: String? = null,
        val loggingEnabled: Boolean = false
    )
}
