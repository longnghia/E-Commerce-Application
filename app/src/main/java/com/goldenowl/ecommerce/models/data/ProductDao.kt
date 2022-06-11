package com.goldenowl.ecommerce.models.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {
    @Query("select * from product_table")
    suspend fun getListProduct(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleProduct(product: List<Product>)

    @Query("DELETE FROM product_table")
    suspend fun deleteTable()


}
