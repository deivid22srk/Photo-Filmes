package com.telegram.videoorganizer.data.model

import com.google.gson.annotations.SerializedName

data class TelegramResponse<T>(
    @SerializedName("ok")
    val ok: Boolean,
    @SerializedName("result")
    val result: T?
)

data class TelegramUpdate(
    @SerializedName("update_id")
    val updateId: Long,
    @SerializedName("message")
    val message: TelegramMessage?
)

data class TelegramMessage(
    @SerializedName("message_id")
    val messageId: Long,
    @SerializedName("from")
    val from: TelegramUser?,
    @SerializedName("chat")
    val chat: TelegramChat,
    @SerializedName("date")
    val date: Long,
    @SerializedName("text")
    val text: String?,
    @SerializedName("video")
    val video: TelegramVideo?
)

data class TelegramUser(
    @SerializedName("id")
    val id: Long,
    @SerializedName("is_bot")
    val isBot: Boolean,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("username")
    val username: String?
)

data class TelegramChat(
    @SerializedName("id")
    val id: Long,
    @SerializedName("type")
    val type: String,
    @SerializedName("title")
    val title: String?
)

data class TelegramVideo(
    @SerializedName("file_id")
    val fileId: String,
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("thumb")
    val thumb: TelegramPhotoSize?,
    @SerializedName("file_name")
    val fileName: String?,
    @SerializedName("mime_type")
    val mimeType: String?,
    @SerializedName("file_size")
    val fileSize: Long?
)

data class TelegramPhotoSize(
    @SerializedName("file_id")
    val fileId: String,
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("file_size")
    val fileSize: Long?
)

data class TelegramFile(
    @SerializedName("file_id")
    val fileId: String,
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    @SerializedName("file_size")
    val fileSize: Long?,
    @SerializedName("file_path")
    val filePath: String?
)
