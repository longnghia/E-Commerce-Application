package com.goldenowl.ecommerce.models.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("select  * from cart_table")
    fun getListCart(): Flow<List<Cart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: Cart)

    @Delete
    suspend fun removeCart(cart: Cart): Int

    @Update
    suspend fun updateCart(cart: Cart)

}