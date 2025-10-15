package com.telegram.videoorganizer.data.api

import com.telegram.videoorganizer.data.model.TelegramFile
import com.telegram.videoorganizer.data.model.TelegramResponse
import com.telegram.videoorganizer.data.model.TelegramUpdate
import com.telegram.videoorganizer.data.model.TelegramUser
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TelegramApi {
    
    @GET("bot{token}/getMe")
    suspend fun getMe(
        @Path(value = "token", encoded = false) token: String
    ): TelegramResponse<TelegramUser>
    
    @GET("bot{token}/getUpdates")
    suspend fun getUpdates(
        @Path(value = "token", encoded = false) token: String,
        @Query("offset") offset: Long? = null,
        @Query("limit") limit: Int? = 100,
        @Query("timeout") timeout: Int? = 0
    ): TelegramResponse<List<TelegramUpdate>>
    
    @GET("bot{token}/getFile")
    suspend fun getFile(
        @Path(value = "token", encoded = false) token: String,
        @Query("file_id") fileId: String
    ): TelegramResponse<TelegramFile>
}
