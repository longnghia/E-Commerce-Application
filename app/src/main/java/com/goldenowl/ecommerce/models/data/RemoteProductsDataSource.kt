package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.models.repo.ProductDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


class RemoteProductsDataSource : ProductDataSource {
    private val db: FirebaseFirestore = Firebase.firestore
    private val observableProducts = MutableLiveData<Result<List<Product>>?>()



    override suspend fun getAllProducts(): List<Product> {
        val collectionRef = db.collection(PRODUCTS_COLLECTION)
        val listProducts = mutableListOf<Product>()
//        val source = Source.CACHE //todo
        val source = Source.SERVER

        val documents = collectionRef.get(source).await()
        if (documents!= null){
            for (d in documents) {
                val product = d.toObject<Product>()
                if (product != null) {
                    listProducts.add(product)
                }
//                Log.d(TAG, "getAllProducts: product=$product")
            }
        }
        else{
            Log.w(TAG, "getAllProducts: Failed" )
        }
        Log.d(TAG, "getAllProducts: result=$listProducts")
        return listProducts
//        collectionRef.get(source).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val document = task.result
//                val documents = document?.documents ?: null
//                if (documents == null) {
//                    Log.w(TAG, "getAllProducts: NULL document")
//                } else {
//                    for (d in documents) {
//                        val product = d.toObject<Product>()
//                        if (product != null) {
//                            listProducts.add(product)
//                        }
//                        Log.d(TAG, "getAllProducts: product=$product")
//                    }
//                }
//            } else {
//                Log.d(TAG, "get failed: ", task.exception)
//            }
//        }
    }


    companion object {
        val TAG = "RemoteData"
        val PRODUCTS_COLLECTION = "products"
        val PROMOTIONS_COLLECTION = "promotions"
        val TAGS_COLLECTION = "tags"

    }
}
