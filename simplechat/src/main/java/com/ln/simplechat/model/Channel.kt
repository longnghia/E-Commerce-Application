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
    var hash: String = ""
) : Parcelable {
    @Exclude
    fun getShortcutId(): String {
        return "$CHANNEL$id"
    }

    @Exclude
    fun getChannelHash(): String {
        val list = listUser.toMutableList()
        list.sort()
        return list.joinToString(USER_SEPERATOR)
    }

    companion object {
        const val CHANNEL = "channel_"
        const val USER_SEPERATOR = "-"
        fun getShortcutId(id: String): String {
            return "$CHANNEL$id"
        }
    }
}

data class ChannelAndMember(
    val channel: Channel,
    val listMember: List<Member>
)