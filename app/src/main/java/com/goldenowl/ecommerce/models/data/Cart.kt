package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "cart_table")
data class Cart @JvmOverloads constructor(
    @PrimaryKey
    val productId: String = "",
    val size: String = "",
    val color: String = "",
    var quantity: Int = 0
): Parcelable
