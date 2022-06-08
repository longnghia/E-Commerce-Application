package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
//                Log.d(TAG, "getAllProducts: product=$product")
            }
        } else {
            Log.w(TAG, "getAllProducts: Failed")
        }
        Log.d(TAG, "getAllProducts: result=$listProducts")
        return listProducts
    }


    override suspend fun addToFavorite(favorite: UserOrder.Favorite, userId: String) {
        withContext(dispatcherIO) {

            if (userId != null) {
                var userRef = db.collection(USER_ORDER_COLLECTION).document(userId)
                    .get().await()
                if (!userRef.exists()) {
                    db.collection(USER_ORDER_COLLECTION).document(userId)
                        .set(UserOrder(userId, listOf(favorite)))
                } else {
                    val userOrder = userRef.toObject(UserOrder::class.java)
                    if (userOrder == null) {
                        Log.w(TAG, "addToFavorite: userorder NULL")

                    }
                    var listFavorite: MutableList<UserOrder.Favorite> =
                        userOrder?.favorites?.toMutableList() ?: mutableListOf()

                    listFavorite.add(favorite)

                    Log.d(TAG, "addToFavorite: $listFavorite")
                    db.collection(USER_ORDER_COLLECTION).document(userId).update("favorites", FieldValue.arrayUnion(favorite)).await()
//                    db.collection(USER_ORDER_COLLECTION).document(userId).update("favorites", listFavorite).await()
                }

            } else {
                Log.d(TAG, "addToFavorite: USER NULL")
            }
        }
    }

    override suspend fun removeFromFavorite(favorite: UserOrder.Favorite, userId: String) {
        Log.d(TAG, "removeFromFavorite")
    }

    override suspend fun insertMultipleProduct(productsList: List<Product>, userId: String) {
//        TODO("Not yet implemented")
    }

    override suspend fun emptyTable() {
        val documents = db.collection(PRODUCTS_COLLECTION).get().await()
        for (d in documents) {
            d.reference.delete()
        }

    }

    override suspend fun insertUserOrder(userOrder: UserOrder) {
//        TODO("Not yet implemented")
    }

    override suspend fun getFavoriteList(userId: String): List<UserOrder.Favorite> {
        return withContext(dispatcherIO) {
            val userOrder = db.collection(USER_ORDER_COLLECTION).document(userId).get().await()
            if (userOrder.exists()) {
                val orderObj = userOrder.toObject(UserOrder::class.java)
                return@withContext orderObj?.favorites ?: emptyList()
            } else
                return@withContext emptyList()
        }
    }


    companion object {
        val TAG = "RemoteData"
        val PRODUCTS_COLLECTION = "products"
        val USER_ORDER_COLLECTION = "user_orders"
        val PROMOTIONS_COLLECTION = "promotions"
        val TAGS_COLLECTION = "tags"

    }
}
