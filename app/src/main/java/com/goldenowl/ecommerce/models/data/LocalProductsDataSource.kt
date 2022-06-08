package com.goldenowl.ecommerce.models.data

import android.util.Log
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalProductsDataSource(private val productDao: ProductDao, private val userOrderDao: UserOrderDao) :
    ProductDataSource {

    private val dispatchers = Dispatchers.IO
    override suspend fun getAllProducts(): List<Product> {
        return listOf()
    }

    override suspend fun addToFavorite(favorite: UserOrder.Favorite, userId: String) {
        withContext(dispatchers) {
            val userOrder = userOrderDao.getUserOrder(userId)
            val listFavorite = userOrder.favorites.toMutableList()

            if (listFavorite.isNullOrEmpty()) {
                Log.d(TAG, "addToFavorite: add to empty list $favorite")
                userOrderDao.insertFavorite(listOf(favorite), userId)
            } else {
                listFavorite.add(favorite)
                Log.d(TAG, "addToFavorite: add to local list (${listFavorite.size}) $favorite")
                userOrderDao.setListFavorites(listFavorite, userId)
            }
        }
    }

    override suspend fun removeFromFavorite(favorite: UserOrder.Favorite, userId: String) {
//        TODO("Not yet implemented")
    }

    override suspend fun insertMultipleProduct(productsList: List<Product>, userId: String) {
        withContext(dispatchers) {

            productDao.insertMultipleProduct(productsList)
        }
    }

    //    private fun getFavorite(userId: String): Flow<List<UserOrder.Favorite>> {
    private suspend fun getFavorite(userId: String): List<UserOrder.Favorite> {
//        return userOrderDao.getListFavorites(userId)
        return withContext(dispatchers) {
            val userOrder = userOrderDao.getUserOrder(userId)
            Log.d(TAG, "getFavorite: ${userOrder.favorites}")
            return@withContext userOrder.favorites
        }
    }


    override suspend fun emptyTable() {
        withContext(dispatchers) {
            productDao.emptyTable()
        }
    }

    override suspend fun insertUserOrder(userOrder: UserOrder) {
        Log.d(TAG, "insertUserOrder: Insert new userorder ${userOrder}")
        withContext(dispatchers) {
//            if (productDao.getUserOrderById(userId) == null)
//                productDao.insertUserOrder(userOrder)

//            if(userOrderDao.getUserOrderById(userOrder.userId) == null){
//                userOrderDao.insertUserOrder((userOrder))
//            }
        }
    }

    override suspend fun getFavoriteList(userId: String): List<UserOrder.Favorite> {
        return withContext(dispatchers) {
            val userOrder = userOrderDao.getUserOrder(userId)
            if (userOrder != null) {
                val listFavorite = userOrder.favorites
                Log.d(TAG, "getFavoriteList: $listFavorite")
                return@withContext listFavorite
            } else {
                Log.d(TAG, "getFavoriteList: userOrder NULL")
            }
            return@withContext emptyList()
        }
    }

    companion object {
        val TAG = "LocalProductsDataSource"
    }
}
