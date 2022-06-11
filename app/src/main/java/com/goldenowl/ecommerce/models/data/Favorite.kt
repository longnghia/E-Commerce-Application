package com.goldenowl.ecommerce.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_table")
data class Favorite @JvmOverloads constructor(
    @PrimaryKey
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


