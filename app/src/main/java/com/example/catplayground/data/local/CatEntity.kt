package com.example.catplayground.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteCatEntity(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val tags: String,
    val mimeType: String?,
    val localFilePath: String?,
    val savedTimestamp: Long
)

@Entity(tableName = "history")
data class HistoryCatEntity(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val tags: String,
    val mimeType: String?,
    val retrievalTimestamp: Long
)
