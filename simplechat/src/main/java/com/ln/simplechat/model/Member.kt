package com.ln.simplechat.model

data class Member(
    var id: String = "",
    var name: String = "",
    val email: String = "",
    var dob: String = "",
    var avatar: String = "",
)