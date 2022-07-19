package com.goldenowl.ecommerce.models.data

data class CartData @JvmOverloads constructor(
    val product: Product,
    val cart: Cart,
    val promo: Promo?
)