package com.telegram.videoorganizer.data.api

import com.telegram.videoorganizer.data.model.TelegramFile
import com.telegram.videoorganizer.data.model.TelegramResponse
import com.telegram.videoorganizer.data.model.TelegramUpdate
import com.telegram.videoorganizer.data.model.TelegramUser
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface TelegramApi {
    
    @GET
    suspend fun getMe(
        @Url url: String
    ): TelegramResponse<TelegramUser>
    
    @GET
    suspend fun getUpdates(
        @Url url: String,
        @Query("offset") offset: Long? = null,
        @Query("limit") limit: Int? = 100,
        @Query("timeout") timeout: Int? = 0
    ): TelegramResponse<List<TelegramUpdate>>
    
    @GET
    suspend fun getFile(
        @Url url: String,
        @Query("file_id") fileId: String
    ): TelegramResponse<TelegramFile>
    
    @GET
    suspend fun getFileWithUrl(
        @Url url: String
    ): TelegramResponse<TelegramFile>
}
