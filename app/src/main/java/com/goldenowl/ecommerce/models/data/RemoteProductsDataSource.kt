package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.goldenowl.ecommerce.models.repo.RemoteAuthDataSource
import com.goldenowl.ecommerce.utils.Consts.PRODUCTS_COLLECTION
import com.goldenowl.ecommerce.utils.Consts.USER_ORDER_COLLECTION
import com.goldenowl.ecommerce.utils.MyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class RemoteProductsDataSource : ProductDataSource {
    private val dispatcherIO: CoroutineContext = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser = firebaseAuth.currentUser
    private val userOrderRef = db.collection(USER_ORDER_COLLECTION)
    private val observableProducts = MutableLiveData<Result<List<Product>>?>()

    override suspend fun getAllProducts(): List<Product> {
        val listProducts = mutableListOf<Product>()
//        val source = Source.CACHE //todo
        val source = Source.SERVER

        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        if (documents != null) {
            for (d in documents) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        } else {
            Log.w(TAG, "getAllProducts: Failed")
        }
        return listProducts
    }


    override suspend fun insertFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get().await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertFavorite: user cart NULL")
            }
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            listFavorite.add(favorite)

            userOrderRef
                .update("favorites", listFavorite)
        }
    }

    override suspend fun removeFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get().await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertFavorite: user cart NULL")
            }
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            listFavorite.add(favorite)
            userOrderRef.update("favorites", listFavorite).await()
        }

    }

    override suspend fun insertMultipleProduct(productsList: List<Product>, userId: String) {
//        TODO("Not yet implemented")
    }

    suspend fun emptyProductTable() {
        val documents = db.collection(PRODUCTS_COLLECTION).get().await()
        for (d in documents) {
            d.reference.delete()
        }

    }


    suspend fun getFavoriteList(userId: String): List<Favorite> {
        return withContext(dispatcherIO) {
            val userOrder = db.collection(USER_ORDER_COLLECTION).document(userId).get().await()
            if (userOrder.exists()) {
                val orderObj = userOrder.toObject(UserOrder::class.java)
                return@withContext orderObj?.favorites ?: emptyList()
            } else
                return@withContext emptyList()
        }
    }

    suspend fun getUserOrder(userId: String): MyResult<UserOrder> {
        Log.d(RemoteAuthDataSource.TAG, "restoreDatabase restoring")
        return withContext(dispatcherIO) {
            try {
                val userDataSnapshot = userOrderRef.document(userId).get().await()
                if (!userDataSnapshot.exists()) {
                    Log.d(RemoteAuthDataSource.TAG, "restoreDatabase: userorder ${userId} not exist")
                    return@withContext MyResult.Error(java.lang.Exception("UserOrder ${userId}  not exist"))
                } else {
                    val userOrder = userDataSnapshot.toObject(UserOrder::class.java)
                        ?: return@withContext MyResult.Error(java.lang.Exception("UserOrder ${userId}  NULL"))

                    Log.d(RemoteAuthDataSource.TAG, "restoreDatabase: $userOrder")
                    return@withContext MyResult.Success(userOrder)
                }
            } catch (e: Exception) {
                Log.e(RemoteAuthDataSource.TAG, "restoreDatabase: ERROR", e)
                return@withContext MyResult.Error(e)
            }
        }
    }


    suspend fun insertCart(cart: Cart) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get().await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), carts = listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertCart: user cart NULL")
            }
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.add(cart)


            userOrderRef
                .update("carts", listCart)
        }
    }

    suspend fun removeCart(cart: Cart) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get().await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertCart: user cart NULL")
            }
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.add(cart)

            userOrderRef
                .update("carts", listCart).await()
        }
    }

    companion object {
        val TAG = "RemoteProductSource"
    }
}
