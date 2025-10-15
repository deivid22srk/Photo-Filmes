package com.telegram.videoorganizer.data.repository

import com.telegram.videoorganizer.data.api.RetrofitClient
import com.telegram.videoorganizer.data.model.Episode
import com.telegram.videoorganizer.data.model.Season
import com.telegram.videoorganizer.data.model.Series
import com.telegram.videoorganizer.data.model.TelegramFile
import com.telegram.videoorganizer.data.model.TelegramUpdate
import com.telegram.videoorganizer.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TelegramRepository {
    
    private val api = RetrofitClient.telegramApi
    
    suspend fun verifyToken(token: String): Result<Boolean> {
        return try {
            Logger.i("TelegramRepository", "Verifying bot token...")
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "https://api.telegram.org/bot$cleanToken/getMe"
            Logger.d("TelegramRepository", "Calling API: $url")
            val response = api.getMe(url)
            if (response.ok && response.result != null) {
                Logger.i("TelegramRepository", "Token verified successfully: ${response.result.firstName}")
                Result.success(true)
            } else {
                Logger.w("TelegramRepository", "Token verification failed: Invalid response")
                Result.failure(Exception("Invalid token"))
            }
        } catch (e: Exception) {
            Logger.e("TelegramRepository", "Token verification failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUpdates(token: String, offset: Long? = null): Result<List<TelegramUpdate>> {
        return try {
            Logger.i("TelegramRepository", "Fetching updates from Telegram...")
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "https://api.telegram.org/bot$cleanToken/getUpdates"
            val response = api.getUpdates(url, offset)
            if (response.ok && response.result != null) {
                Logger.i("TelegramRepository", "Fetched ${response.result.size} updates successfully")
                Result.success(response.result)
            } else {
                Logger.w("TelegramRepository", "Failed to get updates: Invalid response")
                Result.failure(Exception("Failed to get updates"))
            }
        } catch (e: Exception) {
            Logger.e("TelegramRepository", "Failed to fetch updates", e)
            Result.failure(e)
        }
    }
    
    suspend fun getFile(token: String, fileId: String): Result<TelegramFile> {
        return try {
            Logger.i("TelegramRepository", "Getting file info for fileId: $fileId")
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "https://api.telegram.org/bot$cleanToken/getFile?file_id=$fileId"
            Logger.d("TelegramRepository", "Calling getFile API: $url")
            val fileResponse = api.getFileWithUrl(url)
            if (fileResponse.ok && fileResponse.result != null) {
                Logger.i("TelegramRepository", "File info obtained: ${fileResponse.result.filePath}")
                Result.success(fileResponse.result)
            } else {
                Logger.e("TelegramRepository", "Failed to get file: Invalid response")
                Result.failure(Exception("Failed to get file"))
            }
        } catch (e: Exception) {
            Logger.e("TelegramRepository", "Failed to get file", e)
            Result.failure(e)
        }
    }
    
    suspend fun getFileUrl(token: String, fileId: String): Result<String> {
        return try {
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "https://api.telegram.org/bot$cleanToken/getFile"
            val response = api.getFile(url, fileId)
            if (response.ok && response.result?.filePath != null) {
                val downloadUrl = "https://api.telegram.org/file/bot$cleanToken/${response.result.filePath}"
                Result.success(downloadUrl)
            } else {
                Result.failure(Exception("Failed to get file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun parseVideosToSeries(updates: List<TelegramUpdate>): List<Series> {
        Logger.i("TelegramRepository", "Parsing ${updates.size} updates to series...")
        val videoMessages = updates.mapNotNull { it.message }.filter { it.video != null }
        Logger.d("TelegramRepository", "Found ${videoMessages.size} video messages")
        val seriesMap = mutableMapOf<String, MutableMap<Int, MutableList<Episode>>>()
        
        videoMessages.forEach { message ->
            val video = message.video ?: return@forEach
            val text = message.text ?: message.chat.title ?: "Unknown Series"
            
            val regex = """(?i)s(\d+)e(\d+)""".toRegex()
            val match = regex.find(text)
            
            if (match != null) {
                val seasonNum = match.groupValues[1].toIntOrNull() ?: 1
                val episodeNum = match.groupValues[2].toIntOrNull() ?: 1
                val seriesName = text.substringBefore(match.value).trim()
                    .ifEmpty { message.chat.title ?: "Unknown Series" }
                
                val episode = Episode(
                    episodeNumber = episodeNum,
                    title = video.fileName ?: "Episode $episodeNum",
                    fileId = video.fileId,
                    thumbnail = video.thumb?.fileId,
                    duration = video.duration,
                    fileSize = video.fileSize
                )
                
                seriesMap
                    .getOrPut(seriesName) { mutableMapOf() }
                    .getOrPut(seasonNum) { mutableListOf() }
                    .add(episode)
            } else {
                val seriesName = message.chat.title ?: "Unknown Series"
                val episode = Episode(
                    episodeNumber = videoMessages.indexOf(message) + 1,
                    title = video.fileName ?: "Video ${videoMessages.indexOf(message) + 1}",
                    fileId = video.fileId,
                    thumbnail = video.thumb?.fileId,
                    duration = video.duration,
                    fileSize = video.fileSize
                )
                
                seriesMap
                    .getOrPut(seriesName) { mutableMapOf() }
                    .getOrPut(1) { mutableListOf() }
                    .add(episode)
            }
        }
        
        val result = seriesMap.map { (name, seasons) ->
            Series(
                id = name.hashCode().toString(),
                name = name,
                seasons = seasons.map { (seasonNum, episodes) ->
                    Season(
                        seasonNumber = seasonNum,
                        episodes = episodes.sortedBy { it.episodeNumber }
                    )
                }.sortedBy { it.seasonNumber }
            )
        }
        Logger.i("TelegramRepository", "Parsed ${result.size} series with ${result.sumOf { it.seasons.sumOf { s -> s.episodes.size } }} total episodes")
        return result
    }
}
