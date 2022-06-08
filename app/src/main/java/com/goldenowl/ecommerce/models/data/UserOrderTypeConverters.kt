package com.goldenowl.ecommerce.models.data

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserOrderTypeConverters {
    @TypeConverter
    fun listFavoriteToJson(listFavorite: List<UserOrder.Favorite>): String {
        if (listFavorite.isEmpty())
            return ""
        val gson = Gson()
        val type = object : TypeToken<List<UserOrder.Favorite>>() {}.type
        return gson.toJson(listFavorite, type)
    }

    @TypeConverter
    fun jsonToListFavorite(listFavoriteJson: String?): List<UserOrder.Favorite> {
        if (listFavoriteJson.isNullOrBlank())
            return emptyList()
        val gson = Gson()
        val type = object : TypeToken<List<UserOrder.Favorite>>() {}.type
        return gson.fromJson(listFavoriteJson, type)
    }

//    @TypeConverter
//    fun favoriteToJson(favorite: UserOrder.Favorite): String{
//        if (favorite == null)
//            return ""
//        val gson = Gson()
//        val type = object : TypeToken<UserOrder.Favorite>() {}.type
//        return gson.toJson(favorite, type)
//    }
//
//    @TypeConverter
//    fun favoriteFromJson(favorite: String): UserOrder.Favorite?{
//        if (favorite.isNullOrBlank())
//            return null
//        val gson = Gson()
//        val type = object : TypeToken<UserOrder.Favorite>() {}.type
//        Log.d("UserOrderTypeConverters", "favoriteFomJson: $favorite")
//        return gson.fromJson(favorite, type)
//    }
}
