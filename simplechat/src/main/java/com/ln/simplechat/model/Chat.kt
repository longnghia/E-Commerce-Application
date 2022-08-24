package com.ln.simplechat.model

data class Chat(
    val member: Member,
    val message: Message,
    val channelId: String
)