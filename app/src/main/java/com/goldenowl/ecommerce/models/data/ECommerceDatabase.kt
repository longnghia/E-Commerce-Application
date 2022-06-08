package com.goldenowl.ecommerce.models.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserOrder::class, Product::class], version = 1)
@TypeConverters(UserOrderTypeConverters::class, ProductTypeConverters::class)
abstract class ECommerceDatabase : RoomDatabase() {
    abstract fun userOrderDao(): UserOrderDao
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
