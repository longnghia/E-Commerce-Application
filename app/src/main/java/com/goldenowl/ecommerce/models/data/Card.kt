package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import com.goldenowl.ecommerce.utils.RSACipher
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

        fun encrypt(card: Card, rsaCipher: RSACipher): Card {
            return Card(
                rsaCipher.encrypt(card.cardName),
                rsaCipher.encrypt(card.cardNumber),
                rsaCipher.encrypt(card.expireDate),
                rsaCipher.encrypt(card.cvv),
            )
        }

        fun decrypt(encryptedCard: Card, rsaCipher: RSACipher): Card {
            return Card(
                rsaCipher.decrypt(encryptedCard.cardName),
                rsaCipher.decrypt(encryptedCard.cardNumber),
                rsaCipher.decrypt(encryptedCard.expireDate),
                rsaCipher.decrypt(encryptedCard.cvv),
            )
        }
    }
}


