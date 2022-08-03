package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Order
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemoteOrderDataSource(private val userManager: UserManager) {
    private val db: FirebaseFirestore = Firebase.firestore

    var source = Source.DEFAULT

    suspend fun insertOrder(order: Order) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, orders = listOf(order)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listOrder: MutableList<Order> =
                userOrder?.orders?.toMutableList() ?: mutableListOf()

            listOrder.add(order)

            userOrderRef
                .update("orders", listOrder)
        }
    }

    suspend fun removeOrder(order: Order) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, orders = listOf(order)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listOrder: MutableList<Order> =
                userOrder?.orders?.toMutableList() ?: mutableListOf()

            val remoteOrder: Order? = listOrder.find {
                order.trackingNumber == it.trackingNumber
            }

            if (remoteOrder != null)
                listOrder.remove(remoteOrder)
            userOrderRef
                .update("orders", listOrder)
        }
    }

    suspend fun getAllOrder(userId: String): List<Order> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.orders ?: emptyList()
    }

}
