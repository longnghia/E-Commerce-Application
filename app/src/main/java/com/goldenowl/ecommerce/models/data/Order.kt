package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "order_table")
data class Order @JvmOverloads constructor(
    @PrimaryKey
    val id: String = "",
    val trackingNumber: String = "",
    val date: Date,
    val listCart: List<Cart> = emptyList(),
    val promoCode: String = "",

    ) : Parcelable {
    override fun toString(): String {
        return "Order(id='$id', listCart=$listCart)"
    }
}
