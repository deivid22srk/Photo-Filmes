package com.telegram.videoorganizer.data.model

data class Series(
    val id: String,
    val name: String,
    val thumbnail: String? = null,
    val seasons: List<Season> = emptyList()
)

data class Season(
    val seasonNumber: Int,
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val episodeNumber: Int,
    val title: String,
    val fileId: String,
    val thumbnail: String? = null,
    val duration: Int? = null,
    val fileSize: Long? = null
)
