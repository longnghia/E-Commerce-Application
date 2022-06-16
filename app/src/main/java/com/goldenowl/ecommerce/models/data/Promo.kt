package com.goldenowl.ecommerce.models.data

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
//        val current = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//        val formatted = current.format(formatter)
//        println("Current Date and Time is: $formatted")
//        Duration.between(expire, current)
        return 123
    }
}

