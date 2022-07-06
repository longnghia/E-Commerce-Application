package com.goldenowl.ecommerce.models.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrderTypeConverters {
    @TypeConverter
    fun listCartToJson(listCart: List<Cart>): String {
        if (listCart.isNullOrEmpty())
            return ""
        val gson = Gson()
        val type = object : TypeToken<List<Cart>>() {}.type
        return gson.toJson(listCart, type)
    }

    @TypeConverter
    fun listCartFromJson(listCart: String): List<Cart> {
        if (listCart.isNullOrBlank())
            return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<Cart>>() {}.type
        return gson.fromJson(listCart, type)
    }

    @TypeConverter
    fun statusToJson(status: Order.Companion.OrderStatus): String {
        return status.name
    }

    @TypeConverter
    fun statusFromJson(status: String): Order.Companion.OrderStatus {
        return enumValueOf(status)
    }
}
