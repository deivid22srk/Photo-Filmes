package com.telegram.videoorganizer.data.repository

import com.telegram.videoorganizer.data.api.RetrofitClient
import com.telegram.videoorganizer.data.model.Episode
import com.telegram.videoorganizer.data.model.Season
import com.telegram.videoorganizer.data.model.Series
import com.telegram.videoorganizer.data.model.TelegramUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TelegramRepository {
    
    private val api = RetrofitClient.telegramApi
    
    suspend fun verifyToken(token: String): Result<Boolean> {
        return try {
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "bot$cleanToken/getMe"
            val response = api.getMe(url)
            if (response.ok && response.result != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpdates(token: String, offset: Long? = null): Result<List<TelegramUpdate>> {
        return try {
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "bot$cleanToken/getUpdates"
            val response = api.getUpdates(url, offset)
            if (response.ok && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("Failed to get updates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFileUrl(token: String, fileId: String): Result<String> {
        return try {
            val cleanToken = token.trim().replace("\\s+".toRegex(), "")
            val url = "bot$cleanToken/getFile"
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
        val videoMessages = updates.mapNotNull { it.message }.filter { it.video != null }
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
        
        return seriesMap.map { (name, seasons) ->
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
    }
}
