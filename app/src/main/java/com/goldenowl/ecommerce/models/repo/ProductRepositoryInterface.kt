package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder

interface ProductRepositoryInterface {
    fun getCategoryList()
    suspend fun getAllProducts(): List<Product>
    suspend fun addToFavorite(favorite: UserOrder.Favorite)
}
