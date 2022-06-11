package com.goldenowl.ecommerce.models.data

data class ProductData @JvmOverloads constructor(
    val product: Product,
    var favorite: Favorite?,
    var cart: Cart?

) {
    override fun toString(): String {
        return "ProductData(product=${product.id}, favorite=$favorite, cart=$cart)"
    }
}