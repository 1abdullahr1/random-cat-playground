package com.example.catplayground.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catplayground.data.repository.CatRepository
import com.example.catplayground.domain.model.CaptionColor
import com.example.catplayground.domain.model.CaptionPosition
import com.example.catplayground.domain.model.CaptionPresets
import com.example.catplayground.domain.model.VisualFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditorUiState(
    val catId: String = "",
    val imageUrl: String = "",
    val captionText: String = "",
    val position: CaptionPosition = CaptionPosition.BOTTOM,
    val textColor: CaptionColor = CaptionColor.WHITE,
    val filter: VisualFilter = VisualFilter.ORIGINAL,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null
)

class EditorViewModel(
    private val repository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun init(catId: String, imageUrl: String) {
        _uiState.update {
            it.copy(
                catId = catId,
                imageUrl = imageUrl
            )
        }
    }

    fun updateCaption(text: String) {
        _uiState.update { it.copy(captionText = text) }
    }

    fun applyRandomPreset() {
        _uiState.update { it.copy(captionText = CaptionPresets.getRandomPreset()) }
    }

    fun setPosition(position: CaptionPosition) {
        _uiState.update { it.copy(position = position) }
    }

    fun setTextColor(color: CaptionColor) {
        _uiState.update { it.copy(textColor = color) }
    }

    fun setFilter(filter: VisualFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setExporting(exporting: Boolean) {
        _uiState.update { it.copy(isExporting = exporting) }
    }

    fun setSuccessMessage(msg: String?) {
        _uiState.update { it.copy(exportSuccessMessage = msg) }
    }
}
