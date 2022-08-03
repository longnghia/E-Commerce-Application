package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Card
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.goldenowl.ecommerce.utils.MyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemotePaymentDataSource(private val userManager: UserManager) {
    private val db: FirebaseFirestore = Firebase.firestore
    var source = Source.DEFAULT

    suspend fun getListCard(userId: String): MyResult<List<Card>> {
        return try {
            var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
            val snapshot = userOrderRef.get(source).await()
            if (!snapshot.exists()) {
                return MyResult.Success(emptyList())
            } else {
                val userOrder = snapshot.toObject(UserOrder::class.java)
                var listCard: MutableList<Card> =
                    userOrder?.cards?.toMutableList() ?: mutableListOf()
                return MyResult.Success(listCard)
            }
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun insertCard(card: Card) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), cards = listOf(card)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listCard: MutableList<Card> =
                userOrder?.cards?.toMutableList() ?: mutableListOf()

            listCard.add(card)


            userOrderRef
                .update("cards", listCard)
        }
    }

    suspend fun removeCard(position: Int) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), emptyList(), emptyList()))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listCard: MutableList<Card> =
                userOrder?.cards?.toMutableList() ?: mutableListOf()

            listCard.removeAt(position)

            userOrderRef
                .update("cards", listCard).await()
        }
    }

    suspend fun setDefaultCheckOut(default: Map<String, Int?>) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        userOrderRef.update("defaultCheckout", default).await()
    }

    suspend fun getDefaultCheckOut(userId: String): Map<String, Int> {
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.defaultCheckout ?: mapOf()

    }
}
