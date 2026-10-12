package com.example.catplayground.ui.explore

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.catplayground.domain.model.VisualFilter
import com.example.catplayground.image.CatImageProcessor
import com.example.catplayground.image.ImageExporter
import com.example.catplayground.image.ImageSharer
import com.example.catplayground.ui.components.ClayButton
import com.example.catplayground.ui.components.ClayCard
import com.example.catplayground.ui.components.ClayChip
import com.example.catplayground.ui.components.ClayTopBar
import com.example.catplayground.ui.theme.ClayCardShape
import com.example.catplayground.ui.theme.PastelButter
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.PastelMint
import com.example.catplayground.ui.theme.PrimaryPurple
import com.example.catplayground.ui.theme.TextDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditor: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Retry",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. App Top Bar
            ClayTopBar(
                title = "Cat Playground",
                onSettingsClick = onNavigateToSettings
            )

            // 2. Horizontally scrollable row of category chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableTags.forEach { tag ->
                    val isSelected = uiState.selectedTag.equals(tag, ignoreCase = true)
                    ClayChip(
                        text = tag,
                        isSelected = isSelected,
                        onClick = { viewModel.selectTag(tag) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Main Cat Image Stage
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClayCardShape,
                    elevation = 8.dp,
                    backgroundColor = PastelLavender.copy(alpha = 0.4f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ClayCardShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val currentCat = uiState.currentCat
                        if (currentCat != null) {
                            val colorFilter = if (uiState.activeFilter != VisualFilter.ORIGINAL) {
                                ColorFilter.colorMatrix(ColorMatrix(uiState.activeFilter.getColorMatrix()))
                            } else null

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentCat.imageUrl)
                                    .crossfade(220)
                                    .build(),
                                contentDescription = "Adorable cat",
                                contentScale = ContentScale.Crop,
                                colorFilter = colorFilter,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Initial placeholder
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Pets,
                                    contentDescription = null,
                                    tint = PrimaryPurple.copy(alpha = 0.5f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Finding a cat for you...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Loading overlay indicator
                        AnimatedVisibility(
                            visible = uiState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Four Compact Actions: Favorite, Caption, Filter, Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Action
                ActionButton(
                    icon = if (uiState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    label = if (uiState.isFavorite) "Saved" else "Favorite",
                    tint = if (uiState.isFavorite) Color(0xFFFF4081) else TextDark,
                    onClick = { viewModel.toggleFavorite() }
                )

                // Caption Action
                ActionButton(
                    icon = Icons.Rounded.Edit,
                    label = "Caption",
                    tint = PrimaryPurple,
                    onClick = {
                        val cat = uiState.currentCat
                        if (cat != null) {
                            onNavigateToEditor(cat.id, cat.imageUrl)
                        } else {
                            Toast.makeText(context, "Please wait for a cat to load", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Filter Action
                ActionButton(
                    icon = Icons.Rounded.AutoFixHigh,
                    label = uiState.activeFilter.displayName,
                    tint = Color(0xFF2E7D32),
                    onClick = { showFilterSheet = true }
                )

                // Share Action
                ActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    tint = Color(0xFF1976D2),
                    onClick = {
                        val cat = uiState.currentCat
                        if (cat != null) {
                            ImageSharer.shareUrl(context, cat.imageUrl, "Check out this adorable cat from Cat Playground!")
                        }
                    }
                )

                // Download Action
                ActionButton(
                    icon = Icons.Rounded.Download,
                    label = "Save",
                    tint = Color(0xFFE65100),
                    onClick = {
                        val cat = uiState.currentCat
                        if (cat != null) {
                            coroutineScope.launch {
                                Toast.makeText(context, "Saving image...", Toast.LENGTH_SHORT).show()
                                val request = ImageRequest.Builder(context)
                                    .data(cat.imageUrl)
                                    .allowHardware(false)
                                    .build()
                                val result = context.imageLoader.execute(request)
                                val drawable = result.drawable
                                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    val filteredBitmap = CatImageProcessor.applyFilter(bitmap, uiState.activeFilter)
                                    val success = ImageExporter.saveToGallery(context, filteredBitmap)
                                    if (success) {
                                        Toast.makeText(context, "Saved to your Photos gallery!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Could not decode image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. One Prominent, Full-width "Another Cat" Button
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            ) {
                ClayButton(
                    text = if (uiState.isLoading) "Fetching cute cat..." else "Another cat",
                    enabled = !uiState.isLoading,
                    onClick = { viewModel.fetchNextCat() },
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Pets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(90.dp)) // Space for bottom nav
        }

        // Filter Selection Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Visual Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VisualFilter.entries.chunked(3).forEach { columnFilters ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                columnFilters.forEach { filter ->
                                    val isSelected = uiState.activeFilter == filter
                                    ClayChip(
                                        text = filter.displayName,
                                        isSelected = isSelected,
                                        selectedColor = PastelMint,
                                        onClick = {
                                            viewModel.setFilter(filter)
                                            showFilterSheet = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}
