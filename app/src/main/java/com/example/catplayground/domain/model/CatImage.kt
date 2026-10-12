package com.example.catplayground.domain.model

data class CatImage(
    val id: String,
    val imageUrl: String,
    val tags: List<String> = emptyList(),
    val mimeType: String? = null,
    val isFavorite: Boolean = false,
    val isGif: Boolean = false,
    val localFilePath: String? = null
)
