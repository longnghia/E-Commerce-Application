package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemoteCartDataSource(private val userManager: UserManager) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    var source = Source.DEFAULT

    suspend fun insertCart(cart: Cart) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), carts = listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.add(cart)

            userOrderRef
                .update("carts", listCart)

        }
    }

    suspend fun updateCart(cart: Cart, position: Int) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), carts = listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            listCart.removeAt(position)
            listCart.add(position, cart)

            userOrderRef
                .update("carts", listCart)

        }
    }


    suspend fun removeCart(cart: Cart) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), listOf(cart)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listCart: MutableList<Cart> =
                userOrder?.carts?.toMutableList() ?: mutableListOf()

            val remoteCart: Cart? = listCart.find {
                cart.isSame(it)
            }

            if (remoteCart != null)
                listCart.remove(remoteCart)
            userOrderRef
                .update("carts", listCart)
        }
    }


    suspend fun insertMultipleCart(listCart: List<Cart>) {
        listCart.forEach {
            insertCart(it)
        }
    }

    suspend fun emptyCartTable() {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (snapshot.exists()) {
            userOrderRef
                .update("carts", emptyList<Cart>())
        }
    }

    suspend fun getAllCart(userId: String): List<Cart> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.carts ?: emptyList()
    }

}
