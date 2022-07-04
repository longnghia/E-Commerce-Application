package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "address_table")
data class Address @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var fullName: String = "",
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var zipCode: String = "",
    var country: String = "",
) : Parcelable {
    @Exclude
    fun getAddressString(): String? {
        return "${address}\n${city}, $state $zipCode, $country "
    }
    @Exclude
    fun getShippingAddress(): String {
        return "${address}, ${city}, $state $zipCode, $country"
    }
}
