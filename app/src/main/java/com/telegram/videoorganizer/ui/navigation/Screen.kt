package com.telegram.videoorganizer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object SeriesDetail : Screen("series/{seriesId}") {
        fun createRoute(seriesId: String) = "series/$seriesId"
    }
    object VideoPlayer : Screen("player/{seriesId}/{seasonNumber}/{episodeNumber}") {
        fun createRoute(seriesId: String, seasonNumber: Int, episodeNumber: Int) = 
            "player/$seriesId/$seasonNumber/$episodeNumber"
    }
}
