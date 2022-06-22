package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product

interface ProductDataSource {
    suspend fun getAllProducts(): List<Product>
    suspend fun insertFavorite(favorite: Favorite)
    suspend fun removeFavorite(favorite: Favorite)

//    suspend fun emptyProductTable()
//    suspend fun insertUserOrder(userOrder: UserOrder)
//    suspend fun getFavoriteList(userId: String): List<Favorite>
//    suspend fun getUserOrder(userId: String): MyResult<UserOrder>
//    abstract suspend fun updateUserOrder(userOrder: UserOrder): MyResult<Boolean>
}
