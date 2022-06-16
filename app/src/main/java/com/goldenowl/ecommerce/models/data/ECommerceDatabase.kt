package com.goldenowl.ecommerce.models.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Favorite::class, Cart::class, Product::class], version = 1, exportSchema = false)
@TypeConverters(UserOrderTypeConverters::class, ProductTypeConverters::class)
abstract class ECommerceDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): CartDao
    abstract fun productDao(): ProductDao

    companion object {

        @Volatile
        private var INSTANCE: ECommerceDatabase? = null
        fun getDatabase(context: Context): ECommerceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, ECommerceDatabase::class.java, "ecommerce_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
