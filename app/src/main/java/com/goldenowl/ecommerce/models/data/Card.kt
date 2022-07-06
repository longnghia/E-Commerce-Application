package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card @JvmOverloads constructor(
    val cardName: String = "",
    val cardNumber: String = "",
    val expireDate: String = "",
    val cvv: String = ""
) : Parcelable {
    override fun toString(): String {
        return "Card(cardName='$cardName', cardNumber='$cardNumber', expireDate='$expireDate', cvv='$cvv')"
    }

    companion object {
        fun getHiddenNumber(cardNumber: String): String {
            return "**** **** **** ${cardNumber.substring(16)}"
        }
    }
}


