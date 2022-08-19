package com.ln.simplechat.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMedia(
    var path: String = "",
    val duration: Long = 0,
    val mimeType: String = "",
    val description: String = ""
) : Parcelable {
    @Exclude
    fun getMediaType(): MediaType? {
        val type = mimeType.split('/')
        if (type.size == 1)
            return null
        return when (type[0]) {
            "audio" -> MediaType.AUDIO
            "video" -> MediaType.VIDEO
            "image" -> MediaType.IMAGE
            else -> null
        }
    }
}

enum class MediaType {
    IMAGE, VIDEO, AUDIO
}