package com.example.catplayground.ui.editor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.catplayground.domain.model.CaptionColor
import com.example.catplayground.domain.model.CaptionPosition
import com.example.catplayground.domain.model.VisualFilter
import com.example.catplayground.image.CatImageProcessor
import com.example.catplayground.image.ImageExporter
import com.example.catplayground.image.ImageSharer
import com.example.catplayground.ui.components.ClayButton
import com.example.catplayground.ui.components.ClayCard
import com.example.catplayground.ui.components.ClayChip
import com.example.catplayground.ui.theme.ClayButtonShape
import com.example.catplayground.ui.theme.ClayCardShape
import com.example.catplayground.ui.theme.PastelButter
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.PastelMint
import com.example.catplayground.ui.theme.PrimaryPurple
import com.example.catplayground.ui.theme.TextDark
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Text(
                text = "Meme & Caption Editor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Live Preview Canvas Card
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
                backgroundColor = PastelLavender.copy(alpha = 0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(ClayCardShape)
                ) {
                    val colorFilter = if (uiState.filter != VisualFilter.ORIGINAL) {
                        ColorFilter.colorMatrix(ColorMatrix(uiState.filter.getColorMatrix()))
                    } else null

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cat image",
                        contentScale = ContentScale.Crop,
                        colorFilter = colorFilter,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Live Caption Text Overlay Preview
                    if (uiState.captionText.isNotBlank()) {
                        val alignment = when (uiState.position) {
                            CaptionPosition.TOP -> Alignment.TopCenter
                            CaptionPosition.CENTER -> Alignment.Center
                            CaptionPosition.BOTTOM -> Alignment.BottomCenter
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = alignment
                        ) {
                            Text(
                                text = uiState.captionText.uppercase(),
                                color = Color(uiState.textColor.hexColor),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.35f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caption Input Field
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.captionText,
                onValueChange = { viewModel.updateCaption(it) },
                placeholder = { Text("Write your funny caption...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PastelLavender
                ),
                trailingIcon = {
                    if (uiState.captionText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateCaption("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Random Preset Caption Button
        ClayButton(
            text = "Random Fun Caption",
            onClick = { viewModel.applyRandomPreset() },
            backgroundColor = PastelButter,
            contentColor = TextDark,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = TextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Caption Position Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Caption Position",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CaptionPosition.entries.forEach { pos ->
                    ClayChip(
                        text = pos.displayName,
                        isSelected = uiState.position == pos,
                        selectedColor = PastelMint,
                        onClick = { viewModel.setPosition(pos) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text Color Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Text Color",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CaptionColor.entries.forEach { colorOption ->
                    val isSelected = uiState.textColor == colorOption
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(colorOption.hexColor))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PrimaryPurple else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { viewModel.setTextColor(colorOption) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Filter Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Visual Effect",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisualFilter.entries.forEach { filter ->
                    ClayChip(
                        text = filter.displayName,
                        isSelected = uiState.filter == filter,
                        selectedColor = PastelButter,
                        onClick = { viewModel.setFilter(filter) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Export Actions: Save & Share
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save to Photos
            ClayButton(
                text = "Save Meme",
                enabled = !uiState.isExporting,
                onClick = {
                    coroutineScope.launch {
                        viewModel.setExporting(true)
                        val request = ImageRequest.Builder(context)
                            .data(uiState.imageUrl)
                            .allowHardware(false)
                            .build()
                        val result = context.imageLoader.execute(request)
                        val sourceBitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap

                        if (sourceBitmap != null) {
                            val memeBitmap = CatImageProcessor.renderMeme(
                                source = sourceBitmap,
                                caption = uiState.captionText,
                                position = uiState.position,
                                textColor = uiState.textColor,
                                filter = uiState.filter
                            )
                            val saved = ImageExporter.saveToGallery(context, memeBitmap, "CatMeme")
                            viewModel.setExporting(false)
                            if (saved) {
                                Toast.makeText(context, "Meme saved to gallery!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save meme", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.setExporting(false)
                            Toast.makeText(context, "Could not load source image", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                backgroundColor = PrimaryPurple,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Share Meme
            ClayButton(
                text = "Share",
                enabled = !uiState.isExporting,
                onClick = {
                    coroutineScope.launch {
                        viewModel.setExporting(true)
                        val request = ImageRequest.Builder(context)
                            .data(uiState.imageUrl)
                            .allowHardware(false)
                            .build()
                        val result = context.imageLoader.execute(request)
                        val sourceBitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap

                        if (sourceBitmap != null) {
                            val memeBitmap = CatImageProcessor.renderMeme(
                                source = sourceBitmap,
                                caption = uiState.captionText,
                                position = uiState.position,
                                textColor = uiState.textColor,
                                filter = uiState.filter
                            )
                            val shareFile = ImageExporter.saveToCacheForShare(context, memeBitmap)
                            viewModel.setExporting(false)
                            if (shareFile != null) {
                                ImageSharer.shareImageFile(
                                    context,
                                    shareFile,
                                    if (uiState.captionText.isNotBlank()) uiState.captionText else "Made with Cat Playground!"
                                )
                            } else {
                                Toast.makeText(context, "Failed to prepare share file", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.setExporting(false)
                            Toast.makeText(context, "Could not load image to share", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                backgroundColor = PastelMint,
                contentColor = TextDark,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
