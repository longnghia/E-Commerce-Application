package com.goldenowl.ecommerce.models.data

import com.google.firebase.firestore.Exclude
import java.util.*


data class Review @JvmOverloads constructor(
    val userId: String = "",
    val productId: String = "",
    var rating: Int = 0,
    var review: String = "",
    var images: List<String> =  ArrayList(),
    var date: Date = Date(),
)
