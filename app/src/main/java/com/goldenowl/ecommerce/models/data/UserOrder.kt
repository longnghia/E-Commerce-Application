package com.goldenowl.ecommerce.models.data

data class UserOrder @JvmOverloads constructor(
    var userId: String = "",
    var favorites: List<Favorite> = ArrayList(),
    var carts: List<Cart> = ArrayList(),
    var orders: List<Order> = ArrayList(),
    var cards: List<Card> = ArrayList(),
    var addresss: List<Address> = ArrayList(),
    var defaultCheckout: Map<String, Int> = mapOf()
)


//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
//@Entity(tableName = "user_cart_table")
//data class UserOrder @JvmOverloads constructor(
//    @PrimaryKey
//    val userId: String = "",
//    var favorites: List<Favorite> = ArrayList()
//) {
//
//    data class Favorite @JvmOverloads constructor(
//        val productId: String = "",
//        val size: String = "",
//        val color: String = ""
//    ) {
//        fun toMap() = mapOf(
//            "productId" to productId,
//            "size" to size,
//            "color" to color
//        )
//    }
//}
//

