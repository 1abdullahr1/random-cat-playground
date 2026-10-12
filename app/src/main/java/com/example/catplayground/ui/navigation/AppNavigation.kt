package com.example.catplayground.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.catplayground.ui.editor.EditorScreen
import com.example.catplayground.ui.editor.EditorViewModel
import com.example.catplayground.ui.explore.ExploreScreen
import com.example.catplayground.ui.explore.ExploreViewModel
import com.example.catplayground.ui.saved.SavedScreen
import com.example.catplayground.ui.saved.SavedViewModel
import com.example.catplayground.ui.settings.SettingsScreen
import com.example.catplayground.ui.settings.SettingsViewModel
import com.example.catplayground.ui.theme.PastelLavender
import com.example.catplayground.ui.theme.PrimaryPurple
import com.example.catplayground.ui.theme.TextDark
import com.example.catplayground.ui.theme.TextSecondary

@Composable
fun AppNavigation(
    exploreViewModel: ExploreViewModel,
    savedViewModel: SavedViewModel,
    editorViewModel: EditorViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isEditorRoute = currentRoute?.startsWith("editor") == true

    Scaffold(
        bottomBar = {
            if (!isEditorRoute) {
                ClayBottomNavBar(
                    currentRoute = currentRoute ?: NavScreen.Explore.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavScreen.Explore.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.Explore.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(NavScreen.Explore.route) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onNavigateToSettings = { navController.navigate(NavScreen.Settings.route) },
                    onNavigateToEditor = { catId, catUrl ->
                        val encodedUrl = Uri.encode(catUrl)
                        editorViewModel.init(catId, catUrl)
                        navController.navigate("editor/$catId/$encodedUrl")
                    }
                )
            }

            composable(NavScreen.Saved.route) {
                SavedScreen(
                    viewModel = savedViewModel,
                    onNavigateToEditor = { catId, catUrl ->
                        val encodedUrl = Uri.encode(catUrl)
                        editorViewModel.init(catId, catUrl)
                        navController.navigate("editor/$catId/$encodedUrl")
                    }
                )
            }

            composable(NavScreen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(
                route = "editor/{catId}/{catUrl}",
                arguments = listOf(
                    navArgument("catId") { type = NavType.StringType },
                    navArgument("catUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val catId = backStackEntry.arguments?.getString("catId") ?: ""
                val catUrl = backStackEntry.arguments?.getString("catUrl") ?: ""
                val decodedUrl = Uri.decode(catUrl)
                editorViewModel.init(catId, decodedUrl)

                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ClayBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x336050C5), spotColor = Color(0x446050C5))
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavScreen.bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(screen.route) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(PastelLavender)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) PrimaryPurple else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryPurple else TextSecondary
                    )
                }
            }
        }
    }
}
