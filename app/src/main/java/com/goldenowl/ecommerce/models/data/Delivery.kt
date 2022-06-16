package com.goldenowl.ecommerce.models.data

import com.goldenowl.ecommerce.R

data class Delivery @JvmOverloads constructor(
    val id: String = "",
    val time: String = "",
    val logo: Int = R.drawable.ic_fedex
)
