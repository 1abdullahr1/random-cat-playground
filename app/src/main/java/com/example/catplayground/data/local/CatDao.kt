package com.example.catplayground.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteCatEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("SELECT * FROM favorites ORDER BY savedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteCatEntity>>

    @Query("SELECT id FROM favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryCatEntity)

    @Query("SELECT * FROM history ORDER BY retrievalTimestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<HistoryCatEntity>>

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY retrievalTimestamp DESC LIMIT 50)")
    suspend fun trimHistory()

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
