package com.example.catplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.catplayground.ui.editor.EditorViewModel
import com.example.catplayground.ui.explore.ExploreViewModel
import com.example.catplayground.ui.navigation.AppNavigation
import com.example.catplayground.ui.saved.SavedViewModel
import com.example.catplayground.ui.settings.SettingsViewModel
import com.example.catplayground.ui.theme.CatPlaygroundTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as CatPlaygroundApp
        val repository = app.catRepository
        val settingsStore = app.settingsStore

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(settingsStore, repository) as T
                    }
                }
            )

            val themeMode by settingsViewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CatPlaygroundTheme(darkTheme = isDarkTheme) {
                val exploreViewModel: ExploreViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ExploreViewModel(repository) as T
                        }
                    }
                )

                val savedViewModel: SavedViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SavedViewModel(repository) as T
                        }
                    }
                )

                val editorViewModel: EditorViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return EditorViewModel(repository) as T
                        }
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        exploreViewModel = exploreViewModel,
                        savedViewModel = savedViewModel,
                        editorViewModel = editorViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
