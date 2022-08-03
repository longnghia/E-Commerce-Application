package com.goldenowl.ecommerce.models.repo.datasource

import android.util.Log
import com.goldenowl.ecommerce.models.data.AppData
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.Promo
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.APP_DATA_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.PRODUCTS_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.PROMOTIONS_COLLECTION
import com.goldenowl.ecommerce.utils.MyResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemoteProductsDataSource {
    private var lastVisible: DocumentSnapshot? = null
    private val db: FirebaseFirestore = Firebase.firestore
    private val appDataRef = db.collection(APP_DATA_COLLECTION)

    var source = Source.DEFAULT

    suspend fun getAllProducts(): List<Product> {
        val listProducts = mutableListOf<Product>()
        val documents = db.collection(PRODUCTS_COLLECTION).get(source).await()
        if (documents != null) {
            for (d in documents) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }

    suspend fun updateProduct(product: Product) {
        db.collection(PRODUCTS_COLLECTION).document(product.id)
            .set(product)
            .await()
    }

    suspend fun getListPromo(): MyResult<List<Promo>> {
        return try {
            val listPromos = mutableListOf<Promo>()
            val documents = db.collection(PROMOTIONS_COLLECTION).get(source).await()

            for (d in documents) {
                val promo = d.toObject<Promo>()
                listPromos.add(promo)
            }
            MyResult.Success(listPromos)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    private var productRef: Query? = null
    suspend fun loadFirstPage(
        category: String?
    ): MutableList<Product> {
        val listProducts = mutableListOf<Product>()
        lastVisible = null
        Log.d(TAG, "loadFirstPage: category=$category")
        productRef =
            when (category) {
                Constants.KEY_SALE -> {
                    db.collection(PRODUCTS_COLLECTION).whereNotEqualTo("salePercent", null)
                        .orderBy("salePercent", Query.Direction.DESCENDING)
                }
                Constants.KEY_NEW -> {
                    db.collection(PRODUCTS_COLLECTION)
                        .orderBy("createdDate", Query.Direction.DESCENDING)
                }
                "" -> db.collection(PRODUCTS_COLLECTION).orderBy("id")
                null -> db.collection(PRODUCTS_COLLECTION).orderBy("id")
                else -> {
                    db.collection(PRODUCTS_COLLECTION).whereEqualTo("categoryName", category).orderBy("id")
                }
            }

        val querySnapshot = productRef!!
            .limit(Constants.LOAD_MORE_QUANTITY)
            .get(source)
            .await()

        if (querySnapshot.size() > 0)
            lastVisible = querySnapshot.documents[querySnapshot.size() - 1]

        if (querySnapshot != null) {
            for (d in querySnapshot) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }

    suspend fun loadMorePage(
        category: String?
    ): MutableList<Product> {
        val listProducts = mutableListOf<Product>()

        if (productRef == null) return mutableListOf()

        val query = if (lastVisible == null)
            productRef!!
        else
            productRef!!.startAfter(lastVisible!!)
        val querySnapshot = query
            .limit(Constants.LOAD_MORE_QUANTITY)
            .get(source)
            .await()

        if (querySnapshot.size() > 0)
            lastVisible = querySnapshot.documents[querySnapshot.size() - 1]

        if (querySnapshot != null) {
            for (d in querySnapshot) {
                val product = d.toObject<Product>()
                listProducts.add(product)
            }
        }
        return listProducts
    }

    suspend fun getListAppbarImg(): List<Pair<String, String>> {
        Log.d(TAG, "getListAppbarImg: getting list appbar")
        val snapshot = appDataRef.document("homepage")
            .get(source)
            .await()
            .toObject<AppData>()
        val data = snapshot?.homepage ?: emptyList()
        val list = data.sortedBy { it.priority }
        return list.map {
            Pair(it.category, it.img)
        }
    }

    companion object {
        val TAG = "RemoteProductSource"
    }
}
