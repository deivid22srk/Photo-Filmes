package com.telegram.videoorganizer.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.telegram.videoorganizer.data.model.Episode
import com.telegram.videoorganizer.ui.viewmodel.MainViewModel

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    seriesId: String,
    seasonNumber: Int,
    episodeNumber: Int,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    
    val series = uiState.series.find { it.id == seriesId }
    val season = series?.seasons?.find { it.seasonNumber == seasonNumber }
    val episode = season?.episodes?.find { it.episodeNumber == episodeNumber }
    
    if (episode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Episode not found")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    val videoUrl by viewModel.videoUrl.collectAsState()
    val isLoadingUrl by viewModel.isLoadingVideoUrl.collectAsState()
    val videoError by viewModel.videoError.collectAsState()
    var isFullscreen by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            viewModel.clearVideoUrl()
            
            // Restore normal screen orientation and system UI
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.let { act ->
                WindowCompat.getInsetsController(act.window, act.window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }
    
    LaunchedEffect(episode.fileId) {
        viewModel.loadVideoUrl(episode.fileId)
    }
    
    LaunchedEffect(videoUrl) {
        videoUrl?.let { url ->
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }
    
    fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        activity?.let { act ->
            if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                WindowCompat.setDecorFitsSystemWindows(act.window, false)
                WindowCompat.getInsetsController(act.window, act.window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                WindowCompat.getInsetsController(act.window, act.window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }
    
    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when {
                isLoadingUrl -> {
                    LoadingVideoState()
                }
                videoError != null -> {
                    ErrorVideoState(
                        error = videoError ?: "Unknown error",
                        onRetry = { viewModel.retryLoadVideo(episode.fileId) },
                        onBack = onNavigateBack
                    )
                }
                videoUrl != null -> {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                                    // Controller visibility handled by ExoPlayer
                                })
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LoadingVideoState()
                }
            }
            
            IconButton(
                onClick = { toggleFullscreen() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = series?.name ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')} - ${episode.title}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            exoPlayer.release()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { toggleFullscreen() }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
            ) {
                when {
                    isLoadingUrl -> {
                        LoadingVideoState()
                    }
                    videoError != null -> {
                        ErrorVideoState(
                            error = videoError ?: "Unknown error",
                            onRetry = { viewModel.retryLoadVideo(episode.fileId) },
                            onBack = onNavigateBack
                        )
                    }
                    videoUrl != null -> {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LoadingVideoState()
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingVideoState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading video...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorVideoState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error loading video",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}
