package com.goldenowl.ecommerce.models.repo.datasource

interface AuthDataSource {
    fun isLogin(): Boolean
    fun logOut()
    fun getUserId(): String?
}
