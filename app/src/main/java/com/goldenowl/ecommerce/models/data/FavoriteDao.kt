package com.goldenowl.ecommerce.models.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("select  * from favorite_table")
    fun getListFavorite(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun removeFavorite(favorite: Favorite): Int

    @Update
    suspend fun updateFavorite(favorite: Favorite): Int
}