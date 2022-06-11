package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product

interface ProductRepositoryInterface {
    fun getCategoryList()
    suspend fun getAllProducts(): List<Product>
    suspend fun insertFavorite(favorite: Favorite)
}
