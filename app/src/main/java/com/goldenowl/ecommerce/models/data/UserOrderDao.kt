package com.goldenowl.ecommerce.models.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserOrderDao {
    @Query("select  * from user_order_table")
    fun getListUserOrder(): List<UserOrder>

//    @Query("select * from user_order_table where userId= :userId")
//    suspend fun getUserOrderById(userId: String)

    @Query("update user_order_table  set favorites= :favorite where userId= :id")
    fun insertFavorite(favorite: List<UserOrder.Favorite>, id: String)

    @Query("select * from user_order_table where userId= :id")
    suspend fun getUserOrder(id: String): UserOrder

//    @Query("update user_order_table set ")
//    abstract fun updateUserOrder(userId: String, userOrder: UserOrder)

//    @Query("select favorites from user_order_table where userId= :userId")
//    fun getListFavorites(userId: String): Flow<List<UserOrder.Favorite>>
//
    @Query("update user_order_table set favorites= :listFavorite where userId= :userId")
    fun setListFavorites(listFavorite: List<UserOrder.Favorite>, userId: String)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertUserOrder(userOrder: UserOrder)
}