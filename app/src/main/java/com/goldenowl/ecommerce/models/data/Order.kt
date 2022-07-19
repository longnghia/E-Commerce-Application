package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "order_table")
data class Order @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: String = "",
    val trackingNumber: String = "",
    val date: Date = Date(),
    val listCart: List<Cart> = emptyList(),
    val promoCode: String = "",
    val cardId: String = "",
    val totalAmount: Float = 0f,
    val shippingAddress: String = "",
    val delivery: String = "",
    var status: OrderStatus = OrderStatus.PROCESSING

) : Parcelable {
    override fun toString(): String {
        return "Order(id='$id', listCart=$listCart)"
    }

    companion object {
        private const val numberSet = "0123456789"
        private const val charSet = "ABCDEFGHIJKLMNOPQRSTUVWXTZ"
        fun generateOrderId(): String {
            val no = "№"
            val numLength = 7
            val num = (1..numLength)
                .map { numberSet.random() }
                .joinToString("")
            return "$no$num"
        }

        fun generateTrackingNumber(): String {
            val numLength = 2
            val charLength = 10
            val num = (1..numLength)
                .map { numberSet.random() }
                .joinToString("")
            val char = (1..charLength)
                .map { charSet.random() }
                .joinToString("")
            return num + char
        }

        enum class OrderStatus{
            DELIVERED,
            PROCESSING,
            CANCELLED
        }
    }
}
