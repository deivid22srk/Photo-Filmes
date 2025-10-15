package com.telegram.videoorganizer.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    
    private const val LOG_FILE_NAME = "app_logs.txt"
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5 MB
    private const val TAG = "TelegramVideoOrganizer"
    
    private var isEnabled = false
    private var context: Context? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    fun init(context: Context, enabled: Boolean) {
        this.context = context.applicationContext
        this.isEnabled = enabled
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            i("Logger", "Logging enabled")
        }
    }
    
    fun d(tag: String, message: String) {
        log("DEBUG", tag, message)
    }
    
    fun i(tag: String, message: String) {
        log("INFO", tag, message)
    }
    
    fun w(tag: String, message: String) {
        log("WARN", tag, message)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        log("ERROR", tag, fullMessage)
    }
    
    private fun log(level: String, tag: String, message: String) {
        val fullTag = "$TAG:$tag"
        
        when (level) {
            "DEBUG" -> Log.d(fullTag, message)
            "INFO" -> Log.i(fullTag, message)
            "WARN" -> Log.w(fullTag, message)
            "ERROR" -> Log.e(fullTag, message)
        }
        
        if (isEnabled && context != null) {
            writeToFile(level, tag, message)
        }
    }
    
    private fun writeToFile(level: String, tag: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logFile = getLogFile() ?: return@launch
                
                if (logFile.length() > MAX_LOG_SIZE) {
                    rotateLogFile(logFile)
                }
                
                val timestamp = dateFormat.format(Date())
                val logEntry = "$timestamp [$level] $tag: $message\n"
                
                FileWriter(logFile, true).use { writer ->
                    writer.append(logEntry)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write log to file", e)
            }
        }
    }
    
    private fun rotateLogFile(logFile: File) {
        try {
            val backupFile = File(logFile.parent, "app_logs_old.txt")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            logFile.renameTo(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log file", e)
        }
    }
    
    fun getLogFile(): File? {
        return try {
            val logsDir = File(context?.filesDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            File(logsDir, LOG_FILE_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get log file", e)
            null
        }
    }
    
    fun getLogFilePath(): String? {
        return getLogFile()?.absolutePath
    }
    
    fun clearLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getLogFile()?.delete()
                val backupFile = context?.let { File(File(it.filesDir, "logs"), "app_logs_old.txt") }
                backupFile?.delete()
                i("Logger", "Logs cleared")
            } catch (e: Exception) {
                e("Logger", "Failed to clear logs", e)
            }
        }
    }
    
    fun getLogContent(): String {
        return try {
            val logFile = getLogFile()
            if (logFile?.exists() == true) {
                logFile.readText()
            } else {
                "No logs available"
            }
        } catch (e: Exception) {
            "Failed to read logs: ${e.message}"
        }
    }
    
    fun getLogSize(): Long {
        return try {
            getLogFile()?.length() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
