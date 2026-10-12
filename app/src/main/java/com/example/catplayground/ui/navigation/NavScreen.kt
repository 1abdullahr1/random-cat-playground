package com.example.catplayground.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    data object Explore : NavScreen("explore", "Explore", Icons.Rounded.Pets)
    data object Saved : NavScreen("saved", "Saved", Icons.Rounded.Favorite)
    data object Settings : NavScreen("settings", "Settings", Icons.Rounded.Settings)

    companion object {
        val bottomNavItems = listOf(Explore, Saved, Settings)
    }
}
