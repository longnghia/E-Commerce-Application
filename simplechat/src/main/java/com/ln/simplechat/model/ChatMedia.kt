package com.ln.simplechat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMedia(
    var path: String = "",
    val duration: Long = 0,
    val mimeType: String = "",
    val chooseModel: Int = 0,
    val description: String = ""
) : Parcelable