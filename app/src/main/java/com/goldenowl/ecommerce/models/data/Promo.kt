package com.goldenowl.ecommerce.models.data

import com.goldenowl.ecommerce.utils.SimpleDateFormatHelper
import java.util.*


data class Promo @JvmOverloads constructor(
    val salePercent: Int = 0,
    val name: String = "",
    val id: String = "",
    val endDate: Date = Date(),
    val backgroundImage: String = "",
) {
    override fun toString(): String {
        return "Promo(percent=$salePercent, title='$name', code='$id', dayRemain=${getDayRemain()})"
    }

    fun getDayRemain(): Int {
        val left = SimpleDateFormatHelper.countDate(Date(), endDate).toInt()
        return if (left > 0) left else 0
    }
}

