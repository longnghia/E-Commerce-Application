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

