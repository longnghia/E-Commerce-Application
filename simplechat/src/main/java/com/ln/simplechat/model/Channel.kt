package com.ln.simplechat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Channel(
    val id: String = "",
    val name: String? = "",
    var listUser: List<String> = emptyList(),
    val type: String = "pair",
    val icon: String? = null
) : Parcelable