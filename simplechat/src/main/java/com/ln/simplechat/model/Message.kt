package com.ln.simplechat.model

import java.util.*

data class Message @JvmOverloads constructor(
    val sender: String = "",
    val text: String? = null,
    var listImageUrl: List<ChatMedia>? = null,
    val videoUrl: String? = null,
    val timestamp: Date = Date()
)