package com.telegram.videoorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.telegram.videoorganizer.ui.navigation.Screen
import com.telegram.videoorganizer.ui.screens.HomeScreen
import com.telegram.videoorganizer.ui.screens.SeriesDetailScreen
import com.telegram.videoorganizer.ui.screens.SettingsScreen
import com.telegram.videoorganizer.ui.theme.TelegramVideoOrganizerTheme
import com.telegram.videoorganizer.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TelegramVideoOrganizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onNavigateToSeries = { seriesId ->
                                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                                }
                            )
                        }
                        
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(Screen.SeriesDetail.route) { backStackEntry ->
                            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
                            SeriesDetailScreen(
                                seriesId = seriesId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
