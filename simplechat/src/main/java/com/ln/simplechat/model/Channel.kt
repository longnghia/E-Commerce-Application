package com.ln.simplechat.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Channel(
    val id: String = "",
    val name: String = "",
    var listUser: List<String> = emptyList(),
    val type: String = "pair",
    val icon: String? = null,
) : Parcelable {
    @Exclude
    fun getShortcutId(): String {
        return "$CHANNEL$id"
    }

    companion object {
        const val CHANNEL = "channel_"
        fun getShortcutId(id: String): String {
            return "$CHANNEL$id"
        }
    }
}

data class ChannelAndMember(
    val channel: Channel,
    val listMember: List<Member>
)