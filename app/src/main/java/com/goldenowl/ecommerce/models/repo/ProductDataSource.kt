package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder

interface ProductDataSource {
    suspend fun getAllProducts(): List<Product>
    suspend fun addToFavorite(favorite: UserOrder.Favorite, userId: String)
    suspend fun removeFromFavorite(favorite: UserOrder.Favorite, userId: String)
    suspend fun insertMultipleProduct(productsList: List<Product>, userId: String)
    suspend fun emptyTable()
    suspend fun insertUserOrder(userOrder: UserOrder)
    suspend fun getFavoriteList(userId: String): List<UserOrder.Favorite>
}
