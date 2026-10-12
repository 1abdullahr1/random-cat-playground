package com.example.catplayground.ui.explore

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Gif
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.PastelMint
import com.example.catplayground.ui.theme.PrimaryPurple
import com.example.catplayground.ui.theme.TextDark
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { uiState.catFeed.size.coerceAtLeast(1) }
    )

    // Sync pager state changes with ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (uiState.catFeed.isNotEmpty() && page < uiState.catFeed.size) {
                viewModel.onPageChanged(page)
            }
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

            // 2. Photos vs GIFs Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ClayChip(
                    text = "🐱 Photos",
                    isSelected = !uiState.isGifMode,
                    selectedColor = PastelButter,
                    onClick = { viewModel.setGifMode(false) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                ClayChip(
                    text = "🎬 GIFs",
                    isSelected = uiState.isGifMode,
                    selectedColor = PastelMint,
                    onClick = { viewModel.setGifMode(true) }
                )
            }

            // 3. Category Filter Chips
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

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Smooth Sliding Stage (HorizontalPager)
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
                        if (uiState.catFeed.isNotEmpty()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val cat = uiState.catFeed.getOrNull(page)
                                if (cat != null) {
                                    val colorFilter = if (uiState.activeFilter != VisualFilter.ORIGINAL) {
                                        ColorFilter.colorMatrix(ColorMatrix(uiState.activeFilter.getColorMatrix()))
                                    } else null

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(cat.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Adorable cat",
                                        contentScale = ContentScale.Crop,
                                        colorFilter = colorFilter,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        } else {
                            // Shimmer / Placeholder while loading initial feed
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Loading adorable cats...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // First-time Interactive Swipe Tutorial Overlay
                        if (uiState.showSwipeHint && uiState.catFeed.isNotEmpty()) {
                            val infiniteTransition = rememberInfiniteTransition(label = "swipe_hint")
                            val offsetX by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = -24f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "swipe_bounce"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .clickable { viewModel.dismissSwipeHint() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TouchApp,
                                        contentDescription = "Swipe",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Swipe to slide to next cat! 🐾",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Tap to dismiss",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Top-right loading pill if buffer is replenishing
                        if (uiState.isLoading && uiState.catFeed.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(32.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Four Compact Actions: Favorite, Caption, Filter, Share
            val currentCat = uiState.currentCat
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
                    onClick = {
                        val isFav = viewModel.toggleFavorite()
                        val msg = if (isFav) "Added to Favorites! ❤️" else "Removed from Favorites"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                // Caption Action
                ActionButton(
                    icon = Icons.Rounded.Edit,
                    label = "Caption",
                    tint = PrimaryPurple,
                    onClick = {
                        if (currentCat != null) {
                            onNavigateToEditor(currentCat.id, currentCat.imageUrl)
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
                        if (currentCat != null) {
                            ImageSharer.shareUrl(context, currentCat.imageUrl, "Check out this adorable cat from Cat Playground!")
                        }
                    }
                )

                // Download Action
                ActionButton(
                    icon = Icons.Rounded.Download,
                    label = "Save",
                    tint = Color(0xFFE65100),
                    onClick = {
                        if (currentCat != null) {
                            coroutineScope.launch {
                                Toast.makeText(context, "Saving image...", Toast.LENGTH_SHORT).show()
                                val request = ImageRequest.Builder(context)
                                    .data(currentCat.imageUrl)
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

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Prominent "Another Cat" Smooth Slide Button
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            ) {
                ClayButton(
                    text = "Another cat",
                    onClick = {
                        coroutineScope.launch {
                            val nextPage = pagerState.currentPage + 1
                            if (nextPage < uiState.catFeed.size) {
                                pagerState.animateScrollToPage(nextPage)
                            } else {
                                viewModel.fetchMoreCats(2)
                                pagerState.animateScrollToPage(nextPage.coerceAtMost(uiState.catFeed.size - 1))
                            }
                        }
                    },
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

            Spacer(modifier = Modifier.height(90.dp))
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
