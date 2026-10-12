package com.example.catplayground.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PastelLavender,
    onPrimaryContainer = TextDark,
    secondary = PastelMint,
    onSecondary = TextDark,
    tertiary = PastelButter,
    onTertiary = TextDark,
    background = PastelBackground,
    onBackground = TextDark,
    surface = ClayCardSurface,
    onSurface = TextDark,
    surfaceVariant = PastelLavender,
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkText,
    secondary = PastelMint,
    onSecondary = DarkBackground,
    tertiary = PastelButter,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary
)

@Composable
fun CatPlaygroundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
