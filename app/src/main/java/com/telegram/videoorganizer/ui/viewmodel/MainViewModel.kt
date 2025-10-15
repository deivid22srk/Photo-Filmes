package com.telegram.videoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telegram.videoorganizer.data.model.Series
import com.telegram.videoorganizer.data.preferences.PreferencesManager
import com.telegram.videoorganizer.data.repository.TelegramRepository
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
    
    init {
        loadBotToken()
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val result = repository.verifyToken(cleanToken)
            
            result.onSuccess {
                preferencesManager.saveBotToken(cleanToken)
                _uiState.value = _uiState.value.copy(
                    botToken = cleanToken,
                    isConnected = true,
                    isLoading = false,
                    error = null
                )
                loadVideos(cleanToken)
            }.onFailure { error ->
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.getUpdates(token)
            
            result.onSuccess { updates ->
                val series = repository.parseVideosToSeries(updates)
                _uiState.value = _uiState.value.copy(
                    series = series,
                    isLoading = false,
                    error = null
                )
            }.onFailure { error ->
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
    
    data class UiState(
        val botToken: String? = null,
        val isConnected: Boolean = false,
        val isLoading: Boolean = false,
        val series: List<Series> = emptyList(),
        val error: String? = null
    )
}
