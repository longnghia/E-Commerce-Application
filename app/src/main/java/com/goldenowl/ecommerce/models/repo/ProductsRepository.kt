package com.goldenowl.ecommerce.models.repo

import android.util.Log
import com.goldenowl.ecommerce.models.data.*
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope


class ProductsRepository(
    private val remoteProductDataSource: RemoteProductsDataSource,
    private val localProductDataSource: LocalProductsDataSource
) {
    val allFavorite = localProductDataSource.allFavorite
    val allCart = localProductDataSource.allCart


    suspend fun getAllProducts(): List<Product> {
        // todo Room or firebase
        val internetAvailable = true
        return if (internetAvailable)
            remoteProductDataSource.getAllProducts()
        else
            localProductDataSource.getAllProducts()
    }

    suspend fun insertFavorite(favorite: Favorite): MyResult<Boolean> {
        // let 2 child coroutine run async in father  supervisorScope/coroutineScope
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.insertFavorite(favorite) }
                val localSource = async { localProductDataSource.insertFavorite(favorite) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "insertFavorite: ERROR", e)
                MyResult.Error(e)
            }
        }

    }

    suspend fun syncLocalDataSource(productsList: List<Product>, userId: String) {
        localProductDataSource.insertMultipleProduct(productsList, userId)
    }

    suspend fun removeFavorite(favorite: Favorite): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteProductDataSource.removeFavorite(favorite) }
            val localSource = async { localProductDataSource.removeFavorite(favorite) }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun insertCart(cart: Cart): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.insertCart(cart) }
                val localSource = async { localProductDataSource.insertCart(cart) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "insertFavorite: ERROR", e)
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeCart(cart: Cart): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteProductDataSource.removeCart(cart) }
            val localSource = async { localProductDataSource.removeCart(cart) }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

//    suspend fun updateLocalDatabase(userOrder: UserOrder) {
//        localProductDataSource.updateUserOrder(userOrder)
//    }

    companion object {
        val TAG = "ProductsRepository"
    }

}
