package com.goldenowl.ecommerce.models.data

data class User(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val dob: String? = null,
    val password: String? = null,
    val avatar: String? = null,
    val logType: String? = null


) {
    override fun toString(): String {
        return "User(id=$id, name=$name, email=$email, dob=$dob, password=$password, avatar=$avatar, logType=$logType)"
    }
}
