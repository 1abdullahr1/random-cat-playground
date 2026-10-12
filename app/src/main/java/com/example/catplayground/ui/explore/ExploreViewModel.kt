package com.example.catplayground.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catplayground.data.repository.CatRepository
import com.example.catplayground.domain.model.CatImage
import com.example.catplayground.domain.model.VisualFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val currentCat: CatImage? = null,
    val isLoading: Boolean = false,
    val selectedTag: String = "All",
    val availableTags: List<String> = listOf("All", "Cute", "Sleepy", "Orange", "Kitten", "Funny", "GIF"),
    val activeFilter: VisualFilter = VisualFilter.ORIGINAL,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
)

class ExploreViewModel(
    private val repository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null
    private var prefetchJob: Job? = null
    private var upcomingCat: CatImage? = null

    init {
        loadTags()
        fetchNextCat(initial = true)
    }

    private fun loadTags() {
        viewModelScope.launch {
            try {
                val apiTags = repository.getTags()
                val tags = mutableListOf("All")
                tags.addAll(apiTags.map { it.replaceFirstChar { char -> char.uppercase() } })
                if (!tags.contains("GIF")) tags.add("GIF")
                _uiState.update { it.copy(availableTags = tags.distinct()) }
            } catch (e: Exception) {
                // Keep default tags
            }
        }
    }

    fun selectTag(tag: String) {
        if (_uiState.value.selectedTag == tag) return
        _uiState.update { it.copy(selectedTag = tag) }
        upcomingCat = null
        fetchNextCat()
    }

    fun setFilter(filter: VisualFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun fetchNextCat(initial: Boolean = false) {
        if (_uiState.value.isLoading && !initial) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            // If upcoming cat is pre-fetched and matches criteria, display immediately!
            if (upcomingCat != null && !initial) {
                val next = upcomingCat!!
                upcomingCat = null
                _uiState.update {
                    it.copy(
                        currentCat = next,
                        isFavorite = next.isFavorite,
                        errorMessage = null,
                        isLoading = false
                    )
                }
                listenToFavorite(next.id)
                prefetchUpcomingCat()
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val tag = _uiState.value.selectedTag
                val isGif = tag.equals("GIF", ignoreCase = true)
                val filterTag = if (tag == "All" || isGif) null else tag

                val cat = repository.fetchCat(tag = filterTag, isGif = isGif)
                _uiState.update {
                    it.copy(
                        currentCat = cat,
                        isFavorite = cat.isFavorite,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                listenToFavorite(cat.id)
                prefetchUpcomingCat()
            } catch (e: Exception) {
                // Keep existing cat visible and report error clearly!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not fetch new cat: ${e.localizedMessage ?: "Network issue"}"
                    )
                }
            }
        }
    }

    private fun prefetchUpcomingCat() {
        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            try {
                val tag = _uiState.value.selectedTag
                val isGif = tag.equals("GIF", ignoreCase = true)
                val filterTag = if (tag == "All" || isGif) null else tag
                upcomingCat = repository.fetchCat(tag = filterTag, isGif = isGif)
            } catch (e: Exception) {
                upcomingCat = null
            }
        }
    }

    private fun listenToFavorite(catId: String) {
        viewModelScope.launch {
            repository.isFavoriteFlow(catId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        val cat = _uiState.value.currentCat ?: return
        viewModelScope.launch {
            val newStatus = repository.toggleFavorite(cat)
            _uiState.update { it.copy(isFavorite = newStatus) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
