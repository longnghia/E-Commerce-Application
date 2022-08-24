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
    fun getMediaType(): MediaType {
        val type = mimeType.split('/')
        return when (type[0]) {
            MIME_AUDIO -> MediaType.AUDIO
            MIME_VIDEO -> MediaType.VIDEO
            MIME_IMAGE -> MediaType.IMAGE
            else -> MediaType.IMAGE
        }
    }

    companion object {
        const val MIME_AUDIO = "audio"
        const val MIME_VIDEO = "video"
        const val MIME_IMAGE = "image"
    }
}

enum class MediaType {
    IMAGE, VIDEO, AUDIO
}