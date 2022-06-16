package com.goldenowl.ecommerce.models.data

data class UserOrder @JvmOverloads constructor(
    var userId: String = "",
    var favorites: List<Favorite> = ArrayList(),
    var carts: List<Cart> = ArrayList()
)

