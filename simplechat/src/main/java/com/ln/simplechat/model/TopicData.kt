package com.ln.simplechat.model

data class TopicData(
    val channel: Channel,
    val message: Message,
    val sender: Member
)

data class TopicNotificationResult(
    val results: String
)

