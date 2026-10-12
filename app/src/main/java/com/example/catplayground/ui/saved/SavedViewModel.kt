package com.example.catplayground.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catplayground.data.repository.CatRepository
import com.example.catplayground.domain.model.CatImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedUiState(
    val selectedTab: SavedTab = SavedTab.FAVORITES,
    val selectedCatDetail: CatImage? = null
)

enum class SavedTab {
    FAVORITES,
    RECENT
}

class SavedViewModel(
    private val repository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    val favorites: StateFlow<List<CatImage>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<CatImage>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: SavedTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openCatDetail(cat: CatImage) {
        _uiState.update { it.copy(selectedCatDetail = cat) }
    }

    fun dismissCatDetail() {
        _uiState.update { it.copy(selectedCatDetail = null) }
    }

    fun toggleFavorite(cat: CatImage) {
        viewModelScope.launch {
            repository.toggleFavorite(cat)
            if (_uiState.value.selectedCatDetail?.id == cat.id) {
                _uiState.update { state ->
                    state.copy(
                        selectedCatDetail = state.selectedCatDetail?.copy(isFavorite = !cat.isFavorite)
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
