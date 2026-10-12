package com.example.catplayground.ui.saved

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.catplayground.domain.model.CatImage
import com.example.catplayground.image.ImageExporter
import com.example.catplayground.image.ImageSharer
import com.example.catplayground.ui.components.ClayCard
import com.example.catplayground.ui.components.ClayChip
import com.example.catplayground.ui.theme.ClayCardShape
import com.example.catplayground.ui.theme.PastelButter
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.PastelMint
import com.example.catplayground.ui.theme.PrimaryPurple
import com.example.catplayground.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onNavigateToEditor: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val currentList = if (uiState.selectedTab == SavedTab.FAVORITES) favorites else recentHistory

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Cats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState.selectedTab == SavedTab.RECENT && recentHistory.isNotEmpty()) {
                TextButton(
                    onClick = { showClearHistoryDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Clear History",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(
                        text = "Clear",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tabs: Favorites & Recent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClayChip(
                text = "Favorites (${favorites.size})",
                isSelected = uiState.selectedTab == SavedTab.FAVORITES,
                selectedColor = PastelButter,
                onClick = { viewModel.selectTab(SavedTab.FAVORITES) }
            )

            ClayChip(
                text = "Recent (${recentHistory.size})",
                isSelected = uiState.selectedTab == SavedTab.RECENT,
                selectedColor = PastelMint,
                onClick = { viewModel.selectTab(SavedTab.RECENT) }
            )
        }

        // Content Grid or Empty State
        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (uiState.selectedTab == SavedTab.FAVORITES) Icons.Rounded.FavoriteBorder else Icons.Rounded.Pets,
                        contentDescription = null,
                        tint = PrimaryPurple.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (uiState.selectedTab == SavedTab.FAVORITES) "No favorite cats saved yet" else "No recent cats yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Explore and tap the heart to save your favorites!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) {
                items(currentList, key = { it.id }) { cat ->
                    CatGridCard(
                        cat = cat,
                        onClick = { viewModel.openCatDetail(cat) }
                    )
                }
            }
        }
    }

    // Detail Dialog
    uiState.selectedCatDetail?.let { cat ->
        CatDetailDialog(
            cat = cat,
            onDismiss = { viewModel.dismissCatDetail() },
            onToggleFavorite = { viewModel.toggleFavorite(cat) },
            onCaptionClick = {
                viewModel.dismissCatDetail()
                onNavigateToEditor(cat.id, cat.imageUrl)
            },
            onShareClick = {
                ImageSharer.shareUrl(context, cat.imageUrl)
            },
            onDownloadClick = {
                coroutineScope.launch {
                    val request = ImageRequest.Builder(context)
                        .data(cat.localFilePath?.let { File(it) } ?: cat.imageUrl)
                        .allowHardware(false)
                        .build()
                    val result = context.imageLoader.execute(request)
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val saved = ImageExporter.saveToGallery(context, bitmap)
                        if (saved) {
                            Toast.makeText(context, "Saved to your Photos gallery!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Could not save photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    // Clear History Confirmation
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Recent History?") },
            text = { Text("This will remove all recent cat records from your history. Your saved favorites will stay safe.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CatGridCard(
    cat: CatImage,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp,
        backgroundColor = PastelLavender.copy(alpha = 0.35f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageSource = if (cat.localFilePath != null) {
                File(cat.localFilePath)
            } else {
                cat.imageUrl
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageSource)
                    .crossfade(true)
                    .build(),
                contentDescription = "Saved cat",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (cat.isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CatDetailDialog(
    cat: CatImage,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCaptionClick: () -> Unit,
    onShareClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                ClayCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = ClayCardShape,
                    elevation = 6.dp
                ) {
                    val imageSource = if (cat.localFilePath != null) {
                        File(cat.localFilePath)
                    } else {
                        cat.imageUrl
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageSource)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cat detail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (cat.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (cat.isFavorite) Color(0xFFFF4081) else TextDark
                        )
                    }

                    IconButton(onClick = onCaptionClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Caption",
                            tint = PrimaryPurple
                        )
                    }

                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF1976D2)
                        )
                    }

                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Download",
                            tint = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    )
}
