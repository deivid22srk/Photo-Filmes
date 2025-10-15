package com.telegram.videoorganizer.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object SeriesDetail : Screen("series/{seriesId}") {
        fun createRoute(seriesId: String) = "series/$seriesId"
    }
}
