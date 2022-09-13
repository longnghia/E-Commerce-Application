package com.ln.simplechat.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message @JvmOverloads constructor(
    val id: String = "",
    val sender: String = "",
    val text: String? = null,
    var medias: List<ChatMedia>? = null,
    val timestamp: Long = System.currentTimeMillis(),
    @field:JvmField
    var isTimeline: Boolean = false, /* check to add timeline */
    var idleBreak: Boolean = false,  /* check if message is idle */
    @field:JvmField
    var isReact: Boolean = false,
    var reactions: Reaction = Reaction(),
    var quotedMessage: String? = null
) : Parcelable {
    @Exclude
    fun getMessageType(): MessageType {
        if (medias != null)
            return MessageType.MEDIA
        return MessageType.TEXT
    }
}

enum class MessageType {
    TEXT,
    MEDIA
}

enum class MessageState {
    TOP, MIDDLE, BOTTOM, NORMAL
}