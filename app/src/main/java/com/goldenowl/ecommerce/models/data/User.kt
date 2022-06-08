package com.goldenowl.ecommerce.models.data

data class User @JvmOverloads constructor(
    val id: String = "",
    var name: String = "",
    val email: String = "",
    var dob: String = "",
    var password: String = "",
    var avatar: String = "",
    val logType: String = "",
    var settings: Map<String, Boolean> = mapOf()
) {
    override fun toString(): String {
        return "User(id=$id, name=$name, email=$email, dob=$dob, password=$password, avatar=$avatar, logType=$logType)"
    }
}
