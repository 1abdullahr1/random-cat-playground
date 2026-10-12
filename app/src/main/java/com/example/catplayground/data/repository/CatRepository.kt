package com.example.catplayground.data.repository

import android.content.Context
import com.example.catplayground.data.local.CatDao
import com.example.catplayground.data.local.FavoriteCatEntity
import com.example.catplayground.data.local.HistoryCatEntity
import com.example.catplayground.data.remote.CataasApi
import com.example.catplayground.domain.model.CatImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CatRepository(
    private val context: Context,
    private val api: CataasApi,
    private val catDao: CatDao,
    private val okHttpClient: OkHttpClient
) {

    private val defaultTags = listOf(
        "cute", "sleepy", "orange", "kitten", "funny", "black", "playful", "white", "fluffy", "box"
    )

    private var cachedTags: List<String>? = null

    suspend fun getTags(): List<String> {
        cachedTags?.let { return it }
        return try {
            val tags = api.getTags()
                .filter { it.isNotBlank() && it.length in 3..15 && it.all { char -> char.isLetterOrDigit() || char == '-' } }
                .take(30)
            if (tags.isNotEmpty()) {
                cachedTags = tags
                tags
            } else {
                defaultTags
            }
        } catch (e: Exception) {
            defaultTags
        }
    }

    suspend fun fetchCat(tag: String? = null, isGif: Boolean = false): CatImage = withContext(Dispatchers.IO) {
        val dto = try {
            when {
                isGif -> api.getRandomGif()
                tag != null && tag.isNotBlank() && !tag.equals("all", ignoreCase = true) && !tag.equals("random", ignoreCase = true) -> {
                    try {
                        api.getCatByTag(tag)
                    } catch (e: Exception) {
                        try {
                            api.getCatByTag(tag.lowercase())
                        } catch (e2: Exception) {
                            // Automatically fallback to a random cat if tag has no images (never show 404!)
                            api.getRandomCat()
                        }
                    }
                }
                else -> api.getRandomCat()
            }
        } catch (e: Exception) {
            api.getRandomCat()
        }

        val catId = if (dto.finalId.isNotBlank()) dto.finalId else UUID.randomUUID().toString()
        val imageUrl = "https://cataas.com/cat/$catId"
        val isFav = catDao.isFavorite(catId)

        // Record in history
        val historyEntity = HistoryCatEntity(
            id = catId,
            imageUrl = imageUrl,
            tags = dto.tags.joinToString(","),
            mimeType = dto.mimetype,
            retrievalTimestamp = System.currentTimeMillis()
        )
        catDao.insertHistory(historyEntity)
        catDao.trimHistory()

        CatImage(
            id = catId,
            imageUrl = imageUrl,
            tags = dto.tags,
            mimeType = dto.mimetype,
            isFavorite = isFav,
            isGif = isGif || dto.mimetype?.contains("gif", ignoreCase = true) == true
        )
    }

    fun getFavorites(): Flow<List<CatImage>> {
        return catDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                CatImage(
                    id = entity.id,
                    imageUrl = entity.imageUrl,
                    tags = entity.tags.split(",").filter { it.isNotBlank() },
                    mimeType = entity.mimeType,
                    isFavorite = true,
                    isGif = entity.mimeType?.contains("gif", ignoreCase = true) == true,
                    localFilePath = entity.localFilePath
                )
            }
        }
    }

    fun getRecentHistory(): Flow<List<CatImage>> {
        return combine(catDao.getRecentHistory(), catDao.getAllFavoriteIds()) { entities, favIds ->
            val favSet = favIds.toSet()
            entities.map { entity ->
                CatImage(
                    id = entity.id,
                    imageUrl = entity.imageUrl,
                    tags = entity.tags.split(",").filter { it.isNotBlank() },
                    mimeType = entity.mimeType,
                    isFavorite = favSet.contains(entity.id),
                    isGif = entity.mimeType?.contains("gif", ignoreCase = true) == true
                )
            }
        }
    }

    fun isFavoriteFlow(id: String): Flow<Boolean> {
        return catDao.isFavoriteFlow(id)
    }

    suspend fun toggleFavorite(cat: CatImage): Boolean = withContext(Dispatchers.IO) {
        val currentlyFav = catDao.isFavorite(cat.id)
        if (currentlyFav) {
            // Delete favorite entity and local offline image if exists
            catDao.deleteFavorite(cat.id)
            deleteOfflineImage(cat.id)
            false
        } else {
            // Immediately insert favorite record into local database so it is saved instantly!
            val favEntity = FavoriteCatEntity(
                id = cat.id,
                imageUrl = cat.imageUrl,
                tags = cat.tags.joinToString(","),
                mimeType = cat.mimeType,
                localFilePath = null,
                savedTimestamp = System.currentTimeMillis()
            )
            catDao.insertFavorite(favEntity)

            // Cache offline image file
            val localPath = downloadAndCacheLocally(cat.id, cat.imageUrl)
            if (localPath != null) {
                catDao.insertFavorite(favEntity.copy(localFilePath = localPath))
            }
            true
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        catDao.clearHistory()
    }

    private fun downloadAndCacheLocally(catId: String, imageUrl: String): String? {
        return try {
            val favoritesDir = File(context.filesDir, "favorites")
            if (!favoritesDir.exists()) {
                favoritesDir.mkdirs()
            }
            val targetFile = File(favoritesDir, "$catId.jpg")
            if (targetFile.exists()) {
                return targetFile.absolutePath
            }

            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bytes = response.body!!.bytes()
                FileOutputStream(targetFile).use { fos ->
                    fos.write(bytes)
                }
                targetFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteOfflineImage(catId: String) {
        try {
            val favoritesDir = File(context.filesDir, "favorites")
            val targetFile = File(favoritesDir, "$catId.jpg")
            if (targetFile.exists()) {
                targetFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
