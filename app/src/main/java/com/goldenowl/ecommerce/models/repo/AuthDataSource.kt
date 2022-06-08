package com.goldenowl.ecommerce.models.repo

interface AuthDataSource {
    fun isLogin(): Boolean
    fun logOut()
    fun getUserId(): String?
}
