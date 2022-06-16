package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductData @JvmOverloads constructor(
    val product: Product,
    var favorite: Favorite?,
    var cart: Cart?

) : Parcelable {
    override fun toString(): String {
        return "ProductData(product=${product.id}, favorite=$favorite, cart=$cart)"
    }
}