package com.ln.simplechat.model

import java.util.*

data class Message @JvmOverloads constructor(
    val id: String = "",
    val sender: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val timestamp: Date = Date()
)