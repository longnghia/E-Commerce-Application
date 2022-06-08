package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder


class ProductsRepository(
    private val remoteProductDataSource: ProductDataSource,
    private val localProductDataSource: ProductDataSource
) {
    fun getCategoryList() {
    }

    suspend fun getAllProducts(): List<Product> {
        // todo Room or firebase
        val internetAvailable = true
        return if (internetAvailable)
            remoteProductDataSource.getAllProducts()
        else
            localProductDataSource.getAllProducts()
    }

    suspend fun addToFavorite(favorite: UserOrder.Favorite, userId: String) {
        remoteProductDataSource.addToFavorite(favorite, userId)
        localProductDataSource.addToFavorite(favorite, userId)
    }

    suspend fun syncLocalDataSource(productsList: List<Product>, userId: String) {
        localProductDataSource.emptyTable()
        localProductDataSource.insertMultipleProduct(productsList, userId)
    }

    suspend fun createUserOrderTable(userId: String) {
        localProductDataSource.insertUserOrder(UserOrder(userId, emptyList()))
    }

    suspend fun getFavoriteList(userId: String?): List<UserOrder.Favorite> {
        if (userId != null) {
            return localProductDataSource.getFavoriteList(userId)
        }
        return emptyList()
    }

}
