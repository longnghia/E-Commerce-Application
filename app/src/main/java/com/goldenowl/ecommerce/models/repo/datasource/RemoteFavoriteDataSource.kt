package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.PRODUCTS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class RemoteFavoriteDataSource(private val userManager: UserManager) {
    private val dispatcherIO: CoroutineContext = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    var source = Source.DEFAULT


    suspend fun insertFavorite(favorite: Favorite) {
        val userId = if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            listFavorite.add(favorite)

            userOrderRef
                .update("favorites", listFavorite)
        }
    }

    suspend fun removeFavorite(favorite: Favorite) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, listOf(favorite)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listFavorite: MutableList<Favorite> =
                userOrder?.favorites?.toMutableList() ?: mutableListOf()

            val remove = listFavorite.find { it.productId == favorite.productId && it.size == favorite.size }
            listFavorite.remove(remove)
            userOrderRef.update("favorites", listFavorite).await()
        }

    }

    suspend fun emptyProductTable() {
        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        for (d in documents) {
            d.reference.delete()
        }

    }


    suspend fun getFavoriteList(userId: String): List<Favorite> {
        return withContext(dispatcherIO) {
            val userOrder = db.collection(USER_ORDER_COLLECTION).document(userId).get(source).await()
            if (userOrder.exists()) {
                val orderObj = userOrder.toObject(UserOrder::class.java)
                return@withContext orderObj?.favorites ?: emptyList()
            } else
                return@withContext emptyList()
        }
    }

    suspend fun getAllFavorite(userId: String): List<Favorite> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.favorites ?: emptyList()
    }
}
