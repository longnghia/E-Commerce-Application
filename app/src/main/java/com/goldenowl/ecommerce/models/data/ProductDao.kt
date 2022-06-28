package com.goldenowl.ecommerce.models.data

import androidx.room.*

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

    @Query("Select * from product_table where id= :productId")
    suspend fun getProductById(productId: String): Product?

    @Update
    suspend fun updateProduct(product: Product)
}
