package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Address
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemoteAddressDataSource(private val userManager: UserManager) {
    private val db: FirebaseFirestore = Firebase.firestore
    var source = Source.DEFAULT

    suspend fun getListAddress(userId: String): List<Address> {
        val userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        return if (!snapshot.exists()) {
            emptyList()
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            val listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()
            listAddress
        }
    }

    suspend fun insertAddress(address: Address) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        val userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            val listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.add(address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }

    suspend fun removeAddress(position: Int) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        val userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), emptyList(), emptyList()))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            val listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.removeAt(position)
            userOrderRef
                .update("addresss", listAddress).await()
        }
    }

    suspend fun updateAddress(address: Address, position: Int) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        val userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            val listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.removeAt(position)
            listAddress.add(position, address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }
}
