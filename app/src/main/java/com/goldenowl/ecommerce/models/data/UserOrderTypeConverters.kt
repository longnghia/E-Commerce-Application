package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserOrderTypeConverters {
    @TypeConverter
    fun listFavoriteToJson(listFavorite: List<Favorite>): String {
        if (listFavorite.isEmpty())
            return ""
        val gson = Gson()
        val type = object : TypeToken<List<Favorite>>() {}.type
        return gson.toJson(listFavorite, type)
    }

    @TypeConverter
    fun jsonToListFavorite(listFavoriteJson: String?): List<Favorite> {
        if (listFavoriteJson.isNullOrBlank())
            return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<Favorite>>() {}.type
        return gson.fromJson(listFavoriteJson, type)
    }

//    @TypeConverter
//    fun favoriteToJson(favorite: Favorite): String{
//        if (favorite == null)
//            return ""
//        val gson = Gson()
//        val type = object : TypeToken<Favorite>() {}.type
//        return gson.toJson(favorite, type)
//    }
//
//    @TypeConverter
//    fun favoriteFromJson(favorite: String): Favorite?{
//        if (favorite.isNullOrBlank())
//            return null
//        val gson = Gson()
//        val type = object : TypeToken<Favorite>() {}.type
//        Log.d("UserOrderTypeConverters", "favoriteFomJson: $favorite")
//        return gson.fromJson(favorite, type)
//    }
}
