package com.example.catplayground.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catplayground.ui.theme.PastelButter
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.TextDark

@Composable
fun ClayChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = PastelButter,
    unselectedColor: Color = PastelLavender.copy(alpha = 0.5f)
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        label = "chip_bg_color"
    )

    val elevation = if (isSelected) 4.dp else 1.dp

    Box(
        modifier = modifier
            .shadow(elevation, CircleShape)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 36.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
