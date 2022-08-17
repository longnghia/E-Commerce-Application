package com.ln.simplechat.model

import com.google.firebase.firestore.Exclude
import java.util.*

data class Message @JvmOverloads constructor(
    val sender: String = "",
    val text: String? = null,
    var listImageUrl: List<ChatMedia>? = null,
    val videoUrl: String? = null,
    val timestamp: Date = Date()
) {
    @Exclude
    fun getMessageType(): MessageType {
        if (text != null)
            return MessageType.TEXT
        if (listImageUrl != null)
            return MessageType.IMAGE
        if (videoUrl != null)
            return MessageType.VIDEO
        return MessageType.TEXT
    }

}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO
}