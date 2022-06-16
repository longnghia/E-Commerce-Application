package com.goldenowl.ecommerce.models.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Favorite::class, Cart::class,Product::class,Order::class], version = 1, exportSchema = false)
@TypeConverters(ProductTypeConverters::class, OrderTypeConverters::class)
abstract class ECommerceDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao

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
