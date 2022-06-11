package com.goldenowl.ecommerce.models.data

import android.util.Log
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalProductsDataSource(
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao
) : ProductDataSource {

    private val dispatchers = Dispatchers.IO

    val allFavorite = favoriteDao.getListFavorite()
    val allCart = cartDao.getListCart()

    override suspend fun getAllProducts(): List<Product> {
        return listOf()
    }

    override suspend fun insertFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun removeFavorite(favorite: Favorite) {
        favoriteDao.removeFavorite(favorite)
    }

    override suspend fun insertMultipleProduct(productsList: List<Product>, userId: String) {
        withContext(dispatchers) {
            try {
                productDao.deleteTable()
                productDao.insertMultipleProduct(productsList)
            } catch (e: Exception) {
                Log.e(TAG, "insertMultipleProduct: ERROR", e)
            }
        }
    }

    suspend fun observeListFavorite(): MyResult<Flow<List<Favorite>>> {
        return withContext(dispatchers) {
            try {
                val flow = favoriteDao.getListFavorite()
                return@withContext MyResult.Success(flow)
            } catch (e: Exception) {
                return@withContext MyResult.Error(e)
            }
        }
    }

    suspend fun testWait(i: Long) {
        delay(i)
        Log.d(TAG, "testWait: after $i s !!!")
    }

     suspend fun insertCart(cart: Cart) {
        cartDao.insertCart(cart)
    }

     suspend fun removeCart(cart: Cart) {
        cartDao.removeCart(cart)
    }



//    override suspend fun emptyTable() {
//        withContext(dispatchers) {
//            productDao.emptyTable()
//        }
//    }


    companion object {
        val TAG = "LocalProductsDataSource"
    }
}
