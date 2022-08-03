package com.goldenowl.ecommerce.models.repo.datasource

import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.Review
import com.goldenowl.ecommerce.models.data.ReviewData
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.utils.Constants.ERR_NOT_LOGIN
import com.goldenowl.ecommerce.utils.Constants.REVIEW_COLLECTION
import com.goldenowl.ecommerce.utils.Constants.USER_ORDER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.util.*


class RemoteReviewDataSource(private val userManager: UserManager) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val reviewRef = db.collection(REVIEW_COLLECTION)
    private val storageRef = Firebase.storage.reference

    var source = Source.DEFAULT

    suspend fun sendReview(rating: Review): String {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        val reviewRef = db.collection(REVIEW_COLLECTION)
        /* review document */
        val addedReviewRef = reviewRef.add(rating).await()
        return addedReviewRef.id
    }

    suspend fun uploadReviewImage(file: String): String {
        if (firebaseAuth.currentUser == null) throw Exception("[Firebase] Please login first!")
        val stream = FileInputStream(File(file))
        val reviewRef = storageRef.child("images/reviews/${Date().time}")
        stream.let { reviewRef.putStream(it) }.await()
        return reviewRef.downloadUrl.await().toString()
    }

    suspend fun updateHelpful(reviewId: String, helpful: Boolean) {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)

        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()

        val userOrder = snapshot.toObject(UserOrder::class.java)

        var setUsefulReview: MutableSet<String> = userOrder?.usefulReviews?.toMutableSet() ?: mutableSetOf()
        if (helpful)
            setUsefulReview.add(reviewId)
        else
            setUsefulReview.remove(reviewId)
        userOrderRef
            .update("usefulReviews", setUsefulReview.toList())
    }

    suspend fun getReviewByProductId(productId: String): List<ReviewData> {
        val listReviews = mutableListOf<ReviewData>()
        val documents = reviewRef
            .whereEqualTo("productId", productId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get(source).await()
        if (documents != null) {
            for (d in documents) {
                val review = d.toObject<Review>()
                listReviews.add(
                    ReviewData(reviewId = d.id, review = review)
                )
            }
        }
        return listReviews
    }

    suspend fun getHelpfulReview(): List<String> {
        val userId =
            if (userManager.isLoggedIn()) userManager.id else throw NotLoggedInException(ERR_NOT_LOGIN)
        var userOrderRef = db.collection(USER_ORDER_COLLECTION).document(userId)
        val snapshot = userOrderRef.get(source).await()

        val userOrder = snapshot.toObject(UserOrder::class.java) ?: return emptyList()
        return userOrder.usefulReviews
    }

    suspend fun getMyListReview(uid: String): MutableList<Review> {
        val listReviews = mutableListOf<Review>()
        val documents = reviewRef
            .whereEqualTo("userId", uid)
            .get(source).await()
        if (documents != null) {
            for (d in documents) {
                val review = d.toObject<Review>()
                listReviews.add(
                    review
                )
            }
        }
        return listReviews
    }
}
