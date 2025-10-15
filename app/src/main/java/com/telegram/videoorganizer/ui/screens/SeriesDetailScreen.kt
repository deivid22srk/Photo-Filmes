package com.telegram.videoorganizer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.telegram.videoorganizer.data.model.Episode
import com.telegram.videoorganizer.data.model.Season
import com.telegram.videoorganizer.data.model.Series
import com.telegram.videoorganizer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Int, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val series = uiState.series.find { it.id == seriesId }
    
    if (series == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Series not found")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    var selectedSeason by remember { mutableStateOf(series.seasons.firstOrNull()?.seasonNumber ?: 1) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = series.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (series.seasons.size > 1) {
                SeasonSelector(
                    seasons = series.seasons,
                    selectedSeason = selectedSeason,
                    onSeasonSelected = { selectedSeason = it }
                )
            }
            
            val currentSeason = series.seasons.find { it.seasonNumber == selectedSeason }
            if (currentSeason != null) {
                EpisodeList(
                    season = currentSeason,
                    onEpisodeClick = { episodeNumber ->
                        onNavigateToPlayer(selectedSeason, episodeNumber)
                    }
                )
            }
        }
    }
}

@Composable
fun SeasonSelector(
    seasons: List<Season>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = seasons.indexOfFirst { it.seasonNumber == selectedSeason },
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp
    ) {
        seasons.forEach { season ->
            Tab(
                selected = season.seasonNumber == selectedSeason,
                onClick = { onSeasonSelected(season.seasonNumber) },
                text = {
                    Text(
                        text = "Season ${season.seasonNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
    }
}

@Composable
fun EpisodeList(
    season: Season,
    onEpisodeClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(season.episodes) { episode ->
            EpisodeCard(
                episode = episode,
                onClick = { onEpisodeClick(episode.episodeNumber) }
            )
        }
    }
}

@Composable
fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "EP ${episode.episodeNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    episode.duration?.let { duration ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                episode.fileSize?.let { size ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatFileSize(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}
