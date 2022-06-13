package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "favorite_table")
data class Favorite @JvmOverloads constructor(
    @PrimaryKey
    val productId: String = "",
    val size: String = "",
    val color: String = ""
): Parcelable {
    fun toMap() = mapOf(
        "productId" to productId,
        "size" to size,
        "color" to color
    )
}


