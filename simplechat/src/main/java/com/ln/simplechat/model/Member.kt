package com.ln.simplechat.model

import com.google.firebase.firestore.Exclude

data class Member(
    val id: String = "",
    var name: String = "",
    val email: String = "",
    var dob: String = "",
    var avatar: String = "",
) {
    @Exclude
    fun getShortcutId() = "member_$id"
}
