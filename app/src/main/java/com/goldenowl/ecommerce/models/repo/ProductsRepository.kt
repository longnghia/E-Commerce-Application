package com.goldenowl.ecommerce.models.repo

import android.util.Log
import com.goldenowl.ecommerce.models.data.*
import com.goldenowl.ecommerce.models.repo.datasource.*
import com.goldenowl.ecommerce.utils.MyResult
import com.google.firebase.firestore.Source
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope


class ProductsRepository(
    private val remoteProductDataSource: RemoteProductsDataSource,
    private val remoteReviewDataSource: RemoteReviewDataSource,
    private val remoteFavoriteDataSource: RemoteFavoriteDataSource,
    private val remoteOrderDataSource: RemoteOrderDataSource,
    private val remoteAddressDataSource: RemoteAddressDataSource,
    private val remotePaymentDataSource: RemotePaymentDataSource,
    private val remoteCartDataSource: RemoteCartDataSource,

    private val localProductDataSource: LocalProductsDataSource
) {
    private var networkAvailable: Boolean = false
    val allFavorite = localProductDataSource.allFavorite
    val allCart = localProductDataSource.allCart
    val allOrder = localProductDataSource.allOrder


    suspend fun getAllProducts(): List<Product> {
        return remoteProductDataSource.getAllProducts()
    }

    suspend fun insertFavorite(favorite: Favorite): MyResult<Boolean> {
        if (!networkAvailable)
            return MyResult.Error(java.lang.Exception("Network unavailable!"))
        // let 2 child coroutine run async in father  supervisorScope/coroutineScope
        return supervisorScope {
            try {
                val remoteSource = async { remoteFavoriteDataSource.insertFavorite(favorite) }
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

    suspend fun removeFavorite(favorite: Favorite): MyResult<Boolean> {
        if (!networkAvailable)
            return MyResult.Error(java.lang.Exception("Network unavailable!"))

        return supervisorScope {
            try {
                val remoteSource = async { remoteFavoriteDataSource.removeFavorite(favorite) }
                val localSource = async { localProductDataSource.removeFavorite(favorite) }
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
                val remoteSource = async { remoteCartDataSource.insertCart(cart) }
                val localSource = async { localProductDataSource.insertCart(cart) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun updateCart(cart: Cart, position: Int): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteCartDataSource.updateCart(cart, position) }
                val localSource = async { localProductDataSource.updateCart(cart) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeCart(cart: Cart): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteCartDataSource.removeCart(cart) }
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

    suspend fun emptyCartTable(): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteCartDataSource.emptyCartTable() }
            val localSource = async { localProductDataSource.emptyCartTable() }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun insertOrder(order: Order): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteOrderDataSource.insertOrder(order) }
                val localSource = async { localProductDataSource.insertOrder(order) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeOrder(order: Order): MyResult<Boolean> {
        return supervisorScope {
            val remoteSource = async { remoteOrderDataSource.removeOrder(order) }
            val localSource = async { localProductDataSource.removeOrder(order) }
            try {
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun getListPromo(): MyResult<List<Promo>> {
        return remoteProductDataSource.getListPromo()
    }

    suspend fun getListCard(id: String): MyResult<List<Card>> {
        return remotePaymentDataSource.getListCard(id)
    }

    suspend fun getListAddress(id: String): MyResult<List<Address>> {
        return try {
            val data = remoteAddressDataSource.getListAddress(id)
            MyResult.Success(data)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertCard(card: Card): MyResult<Boolean> {
        return try {
            remotePaymentDataSource.insertCard(card)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun setDefaultCheckOut(default: Map<String, Int?>): MyResult<Boolean> {
        return try {
            remotePaymentDataSource.setDefaultCheckOut(default)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun removeCard(position: Int): MyResult<Boolean> {
        return try {
            remotePaymentDataSource.removeCard(position)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getDefaultCheckOut(id: String): MyResult<Map<String, Int>> {
        return try {
            val data = remotePaymentDataSource.getDefaultCheckOut(id)
            MyResult.Success(data)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertAddress(address: Address): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remote = async { remoteAddressDataSource.insertAddress(address) }
                val local = async { localProductDataSource.insertAddress(address) }
                remote.await()
                local.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun removeAddress(position: Int, address: Address): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remote = async { remoteAddressDataSource.removeAddress(position) }
                val local = async { localProductDataSource.removeAddress(address) }
                remote.await()
                local.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun updateAddress(address: Address, position: Int): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteAddressDataSource.updateAddress(address, position) }
                val localSource = async { localProductDataSource.updateAddress(address) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    fun setNetworkAvailable(networkAvailable: Boolean) {
        this.networkAvailable = networkAvailable
    }

    suspend fun uploadReviewImages(list: List<String>): MyResult<List<String>> {
        if (list.isEmpty())
            return MyResult.Success(emptyList())
        return supervisorScope {
            val listUrl: MutableList<String> = ArrayList()
            try {
                for (file in list) {
                    val link = async { remoteReviewDataSource.uploadReviewImage(file) }
                    listUrl.add(link.await())
                }
                MyResult.Success(listUrl)

            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun sendReview(rating: Review): MyResult<String> {
        return try {
            val reviewId = remoteReviewDataSource.sendReview(rating)
            MyResult.Success(reviewId)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getProductById(productId: String): MyResult<Product?> {
        return try {
            MyResult.Success(localProductDataSource.getProductById(productId))
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun updateHelpful(reviewId: String, helpful: Boolean): MyResult<Boolean> {
        return try {
            remoteReviewDataSource.updateHelpful(reviewId, helpful)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getReviewByProductId(productId: String): MyResult<List<ReviewData>> {
        return try {
            val listReview = remoteReviewDataSource.getReviewByProductId(productId)
            MyResult.Success(listReview)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getHelpfulReview(): MyResult<List<String>> {
        return try {
            val listReview: List<String> = remoteReviewDataSource.getHelpfulReview()
            MyResult.Success(listReview)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun updateProduct(product: Product): MyResult<Boolean> {
        return supervisorScope {
            try {
                val remoteSource = async { remoteProductDataSource.updateProduct(product) }
                val localSource = async { localProductDataSource.updateProduct(product) }
                remoteSource.await()
                localSource.await()
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun restoreCart(mOrder: Order): MyResult<Boolean> {
        return supervisorScope {
            try {
                localProductDataSource.insertMultipleCart(mOrder.listCart)
                remoteCartDataSource.insertMultipleCart(mOrder.listCart)
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun restoreUserData(userId: String): MyResult<Boolean> {
        return supervisorScope {
            try {
                val favoriteRes = async {
                    val allFavorite = remoteFavoriteDataSource.getAllFavorite(userId)
                    if (allFavorite.isNotEmpty())
                        localProductDataSource.insertMultipleFavorite(allFavorite)
                }
                val cartRes = async {
                    val allCart = remoteCartDataSource.getAllCart(userId)
                    if (allCart.isNotEmpty())
                        localProductDataSource.insertMultipleCart(allCart)
                }
                val orderRes = async {
                    val allOrder = remoteOrderDataSource.getAllOrder(userId)
                    if (allOrder.isNotEmpty())
                        localProductDataSource.insertMultipleOrder(allOrder)
                }
                val listFavorite = favoriteRes.await()
                val listCart = cartRes.await()
                val listOrder = orderRes.await()
                Log.d(TAG, "restoreUserData: listFavorite=\n$listFavorite \nlistCart=$listCart \nlistOrder=$listOrder")
                MyResult.Success(true)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }

    suspend fun clearLocalDataSource(): MyResult<Boolean> {
        return try {
            localProductDataSource.clearLocalDataSource()
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getMyListReview(uid: String): MyResult<List<Review>> {
        return try {
            val listReview = remoteReviewDataSource.getMyListReview(uid)
            MyResult.Success(listReview)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun loadFirstPage(category: String?): MyResult<MutableList<Product>> {
        return try {
            MyResult.Success(remoteProductDataSource.loadFirstPage(category))
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun loadMorePage(category: String?): MyResult<MutableList<Product>> {
        return try {
            MyResult.Success(remoteProductDataSource.loadMorePage(category))
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getListAppbarImg(): MyResult<List<Pair<String, String>>> {
        return try {
            MyResult.Success(remoteProductDataSource.getListAppbarImg())
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    fun setNetWorkAvailable(network: Boolean) {
        remoteProductDataSource.source = if (network) Source.DEFAULT else Source.CACHE
    }

    companion object {
        val TAG = "ProductsRepository"
    }

}
