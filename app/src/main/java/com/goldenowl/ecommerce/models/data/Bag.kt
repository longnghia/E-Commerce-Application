package com.goldenowl.ecommerce.models.data

data class Bag @JvmOverloads constructor(
    var listCart: List<Cart> = emptyList(),
    var promo: Promo? = null,
    var orderPrice: Float = 0f
)