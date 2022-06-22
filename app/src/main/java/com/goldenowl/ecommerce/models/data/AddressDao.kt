package com.goldenowl.ecommerce.models.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("select  * from address_table")
    fun getListAddress(): Flow<List<Address>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: Address)

    @Delete
    suspend fun removeAddress(address: Address): Int

    @Update
    suspend fun updateAddress(address: Address)

}