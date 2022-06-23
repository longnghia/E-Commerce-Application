package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.goldenowl.ecommerce.models.repo.RemoteAuthDataSource
import com.goldenowl.ecommerce.utils.Constants.PRODUCTS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.PROMOTIONS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
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
        val source = Source.SERVER

        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        if (documents != null) {
            for (d in documents) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }


    override suspend fun insertFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

//            userOrderRef
//                .update("favorites", FieldValue.arrayUnion(favorite))
            userOrderRef
                .update("favorites", listFavorite)
            Log.d(TAG, "added To list Favorite")
        }
    }

    override suspend fun removeFavorite(favorite: Favorite) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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
            Log.d(TAG, "removeFavorite")
        }

    }

    suspend fun emptyProductTable() {
        val documents = db.collection(PRODUCTS_COLLECTION).get(Source.SERVER).await()
        for (d in documents) {
            d.reference.delete()
        }

    }


    suspend fun getFavoriteList(userId: String): List<Favorite> {
        return withContext(dispatcherIO) {
            val userOrder = db.collection(USER_ORDER_COLLECTION).document(userId).get(Source.SERVER).await()
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
                val userDataSnapshot = userOrderRef.document(userId).get(Source.SERVER).await()
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
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

            Log.d(TAG, "added To list Cart")
        }
    }

    suspend fun updateCart(cart: Cart, position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

            listCart.removeAt(position)
            listCart.add(position, cart)

            userOrderRef
                .update("carts", listCart)

        }
    }


    suspend fun removeCart(cart: Cart) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

            val remoteCart: Cart? = listCart.find {
                cart.isSame(it)
            }

            if (remoteCart != null)
                listCart.remove(remoteCart)
            userOrderRef
                .update("carts", listCart)
        }
    }

    suspend fun emptyCartTable() {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        if (snapshot.exists()) {
            userOrderRef
                .update("carts", emptyList<Cart>())
        }
    }

    suspend fun insertOrder(order: Order) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

    suspend fun getListPromo(): MyResult<List<Promo>> {
        return try {
            val listPromos = mutableListOf<Promo>()
            val documents = db.collection(PROMOTIONS_COLLECTION).get().await()

            for (d in documents) {
                val promo = d.toObject<Promo>()
                listPromos.add(promo)
            }
            MyResult.Success(listPromos)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    suspend fun getListCard(): MyResult<List<Card>> {
        return try {
            val userId =
                firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
            var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
            val snapshot = userOrderRef.get(Source.SERVER).await()
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
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
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

    suspend fun getListAddress(): List<Address> {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        if (!snapshot.exists()) {
            return emptyList()
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()
            return listAddress
        }
    }

    suspend fun setDefaultCheckOut(default: Map<String, Int?>) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        userOrderRef.update("defaultCheckout", default).await()
    }

    suspend fun getDefaultCheckOut(): Map<String, Int> {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        val userOrder = snapshot.toObject(UserOrder::class.java)
        return userOrder?.defaultCheckout ?: mapOf()

    }

    /* user address */
    suspend fun insertAddress(address: Address) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.add(address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }

    suspend fun removeAddress(position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw (Exception("[Firestore] User not logged in!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, emptyList(), emptyList(), emptyList()))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)

            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.removeAt(position)

            userOrderRef
                .update("addresss", listAddress).await()
        }
    }

    suspend fun updateAddress(address: Address, position: Int) {
        val userId =
            firebaseAuth.currentUser?.uid ?: throw(java.lang.Exception("[Firestore] User not found!"))
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(Source.SERVER).await()
        if (!snapshot.exists()) {
            userOrderRef
                .set(UserOrder(userId, favorites = emptyList(), addresss = listOf(address)))
        } else {
            val userOrder = snapshot.toObject(UserOrder::class.java)
            if (userOrder == null) {
                Log.w(TAG, "insertAddress: user address NULL")
            }
            var listAddress: MutableList<Address> =
                userOrder?.addresss?.toMutableList() ?: mutableListOf()

            listAddress.removeAt(position)
            listAddress.add(position, address)

            userOrderRef
                .update("addresss", listAddress)
        }
    }

    companion object {
        val TAG = "RemoteProductSource"
    }
}
