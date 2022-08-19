package com.ln.simplechat.model

import com.google.firebase.firestore.Exclude

data class Message @JvmOverloads constructor(
    val id: String = "",
    val sender: String = "",
    val text: String? = null,
    var medias: List<ChatMedia>? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
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