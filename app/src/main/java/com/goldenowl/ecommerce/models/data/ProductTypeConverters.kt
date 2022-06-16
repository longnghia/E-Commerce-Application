package com.goldenowl.ecommerce.models.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ProductTypeConverters {
    @TypeConverter
    fun listColorToJson(listColor: List<Product.Color>): String {
        if (listColor.isNullOrEmpty())
            return ""
        val gson = Gson()
        val type = object : TypeToken<List<Product.Color>>() {}.type
        return gson.toJson(listColor, type)
    }

    @TypeConverter
    fun listColorFromJson(listColor: String): List<Product.Color> {
        if (listColor.isNullOrBlank())
            return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<Product.Color>>() {}.type
        return gson.fromJson(listColor, type)
    }

    @TypeConverter
    fun listTagToJson(listTag: List<Product.Tag>): String {
        if (listTag.isNullOrEmpty())
            return ""
        val gson = Gson()
        val type = object : TypeToken<List<Product.Tag>>() {}.type
        return gson.toJson(listTag, type)
    }

    @TypeConverter
    fun listTagFromJson(listTag: String): List<Product.Tag> {
        if (listTag.isNullOrBlank())
            return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<Product.Tag>>() {}.type
        return gson.fromJson(listTag, type)
    }

    @TypeConverter
    fun dateFromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun listImageToString(listString: List<String>): String {
        if (listString.isNullOrEmpty())
            return ""
        return listString.joinToString(",")
    }

    @TypeConverter
    fun listImageFromString(listString: String): List<String> {
        if (listString.isNullOrBlank())
            return emptyList()
        return listString.split(",").map { it }
    }

    @TypeConverter
    fun tagToJson(tag: Product.Tag): String {
        if (tag == null)
            return ""
        val gson = Gson()
        val type = object : TypeToken<Product.Tag>() {}.type
        return gson.toJson(tag, type)
    }

    @TypeConverter
    fun tagFromJson(tag: String): Product.Tag {
        if (tag.isNullOrBlank())
            return Product.Tag("", "")
        val gson = Gson()
        val type = object : TypeToken<Product.Tag>() {}.type
        return gson.fromJson(tag, type)
    }

    @TypeConverter
    fun colorToJson(color: Product.Color): String {
        if (color == null)
            return ""
        val gson = Gson()
        val type = object : TypeToken<Product.Color>() {}.type
        return gson.toJson(color, type)
    }

    @TypeConverter
    fun colorFromJson(color: String): Product.Color {
        if (color.isNullOrBlank())
            return Product.Color("", emptyList())
        val gson = Gson()
        val type = object : TypeToken<Product.Color>() {}.type
        return gson.fromJson(color, type)
    }
}
