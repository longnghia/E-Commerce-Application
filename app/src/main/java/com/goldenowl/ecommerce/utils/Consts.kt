package com.goldenowl.ecommerce.utils

import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Delivery

object Consts {
    const val PRODUCTS_COLLECTION = "products"
    const val USER_ORDER_COLLECTION = "user_orders"
    const val USER_DATA_COLLECTION = "users"
    const val PROMOTIONS_COLLECTION = "promotions"
    const val TAGS_COLLECTION = "tags"
    const val FAVORITE_COLLECTION = "favorites"

    const val GRID_VIEW = 1
    const val LIST_VIEW = 0
    const val SPAN_COUNT_ONE = 1
    const val SPAN_COUNT_TWO = 2

    val sortMap = mapOf(
        SortType.POPULAR to R.string.sort_by_popular,
        SortType.NEWEST to R.string.sort_by_newest,
        SortType.PRICE_INCREASE to R.string.sort_by_price_low_2_high,
        SortType.PRICE_DECREASE to R.string.sort_by_price_high_2_low,
        SortType.REVIEW to R.string.sort_by_customer_review
    )

    val colorMap = mapOf(
        "red" to R.color.red_dark,
        "black" to R.color.black_light,
        "grey" to R.color.grey_text,
    )

    val listSize = listOf("L", "M", "S", "XL", "XS")
    val listDelivery = listOf<Delivery>(
        Delivery("fedex", " 2-3 days", R.drawable.ic_fedex),
        Delivery("usps", " 2-3 days", R.drawable.ic_usps),
        Delivery("dhl", " 2-3 days", R.drawable.ic_dhl),
        Delivery("dhl", " 2-3 days", R.drawable.ic_dhl),
        Delivery("dhl", " 2-3 days", R.drawable.ic_dhl),
    )

}