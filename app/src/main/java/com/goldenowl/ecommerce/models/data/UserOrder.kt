package com.goldenowl.ecommerce.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_order_table")
data class UserOrder @JvmOverloads constructor(
    @PrimaryKey
    val userId: String = "",
    var favorites: List<Favorite> = ArrayList()
) {

    data class Favorite @JvmOverloads constructor(
        val productId: String = "",
        val size: String = "",
        val color: String = ""
    ) {
        fun toMap() = mapOf(
            "productId" to productId,
            "size" to size,
            "color" to color
        )
    }
}

