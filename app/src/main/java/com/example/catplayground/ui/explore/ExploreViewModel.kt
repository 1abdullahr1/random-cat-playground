package com.example.catplayground.ui.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.catplayground.data.repository.CatRepository
import com.example.catplayground.domain.model.CatImage
import com.example.catplayground.domain.model.VisualFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ExploreUiState(
    val catFeed: List<CatImage> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val selectedTag: String = "All",
    val availableTags: List<String> = listOf("All", "Cute", "Sleepy", "Orange", "Kitten", "Funny"),
    val activeFilter: VisualFilter = VisualFilter.ORIGINAL,
    val isGifMode: Boolean = false,
    val isFavorite: Boolean = false,
    val showSwipeHint: Boolean = true
) {
    val currentCat: CatImage?
        get() = catFeed.getOrNull(currentIndex)
}

class ExploreViewModel(
    private val repository: CatRepository,
    private val appContext: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var feedJob: Job? = null
    private var favoriteJob: Job? = null
    private var isPrefetching = false

    init {
        loadTags()
        reloadFeed()
    }

    private fun loadTags() {
        viewModelScope.launch {
            try {
                val apiTags = repository.getTags()
                val tags = mutableListOf("All")
                tags.addAll(apiTags.map { it.replaceFirstChar { char -> char.uppercase() } })
                _uiState.update { it.copy(availableTags = tags.distinct()) }
            } catch (e: Exception) {
                // Keep default tags
            }
        }
    }

    fun selectTag(tag: String) {
        if (_uiState.value.selectedTag == tag) return
        _uiState.update { it.copy(selectedTag = tag) }
        reloadFeed()
    }

    fun setGifMode(isGif: Boolean) {
        if (_uiState.value.isGifMode == isGif) return
        _uiState.update { it.copy(isGifMode = isGif) }
        reloadFeed()
    }

    fun setFilter(filter: VisualFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onPageChanged(newIndex: Int) {
        _uiState.update { it.copy(currentIndex = newIndex, showSwipeHint = false) }
        val cat = _uiState.value.catFeed.getOrNull(newIndex)
        if (cat != null) {
            listenToFavorite(cat.id)
        }
        // If nearing end of loaded buffer, automatically fetch more ahead!
        if (newIndex >= _uiState.value.catFeed.size - 3) {
            fetchMoreCats()
        }
    }

    fun dismissSwipeHint() {
        _uiState.update { it.copy(showSwipeHint = false) }
    }

    fun reloadFeed() {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, catFeed = emptyList(), currentIndex = 0) }
            val initialBatch = mutableListOf<CatImage>()
            val tag = _uiState.value.selectedTag
            val isGif = _uiState.value.isGifMode
            val filterTag = if (tag.equals("All", ignoreCase = true)) null else tag

            for (i in 0 until 3) {
                try {
                    val cat = repository.fetchCat(tag = filterTag, isGif = isGif)
                    initialBatch.add(cat)
                    preloadImage(cat.imageUrl)
                } catch (e: Exception) {
                    // Transparent fallback
                }
            }

            if (initialBatch.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        catFeed = initialBatch,
                        isLoading = false,
                        isFavorite = initialBatch.first().isFavorite
                    )
                }
                listenToFavorite(initialBatch.first().id)
                fetchMoreCats()
            } else {
                // Guaranteed fallback
                try {
                    val fallback = repository.fetchCat(tag = null, isGif = false)
                    initialBatch.add(fallback)
                    preloadImage(fallback.imageUrl)
                    _uiState.update {
                        it.copy(
                            catFeed = initialBatch,
                            isLoading = false,
                            isFavorite = fallback.isFavorite
                        )
                    }
                    listenToFavorite(fallback.id)
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun fetchMoreCats(count: Int = 3) {
        if (isPrefetching) return
        isPrefetching = true
        viewModelScope.launch {
            try {
                val tag = _uiState.value.selectedTag
                val isGif = _uiState.value.isGifMode
                val filterTag = if (tag.equals("All", ignoreCase = true)) null else tag

                val newCats = mutableListOf<CatImage>()
                for (i in 0 until count) {
                    try {
                        val cat = repository.fetchCat(tag = filterTag, isGif = isGif)
                        newCats.add(cat)
                        preloadImage(cat.imageUrl)
                    } catch (e: Exception) {
                        // Silent fallback
                    }
                }
                if (newCats.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(catFeed = (state.catFeed + newCats).distinctBy { it.id })
                    }
                }
            } finally {
                isPrefetching = false
            }
        }
    }

    private fun preloadImage(url: String) {
        appContext?.let { context ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .crossfade(true)
                        .build()
                    context.imageLoader.enqueue(request)
                } catch (e: Exception) {
                    // Ignore preloading issues
                }
            }
        }
    }

    private fun listenToFavorite(catId: String) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            repository.isFavoriteFlow(catId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite(): Boolean {
        val cat = _uiState.value.currentCat ?: return false
        val newFavState = !cat.isFavorite
        viewModelScope.launch {
            repository.toggleFavorite(cat)
            _uiState.update { state ->
                val updatedFeed = state.catFeed.map {
                    if (it.id == cat.id) it.copy(isFavorite = newFavState) else it
                }
                state.copy(
                    catFeed = updatedFeed,
                    isFavorite = newFavState
                )
            }
        }
        return newFavState
    }
}
