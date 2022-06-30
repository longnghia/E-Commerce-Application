package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.PasswordUtils

class LocalAuthDataSource(private val userManager: UserManager) : AuthDataSource {
    override fun isLogin(): Boolean {
        return userManager.isLoggedIn()
    }

    override fun logOut() {
        userManager.logOut()
    }

    override fun getUserId(): String {
        return userManager.id
    }

    fun updateUserData(user: User) {
        userManager.apply {
            this.name = user.name
            this.dob = user.dob
            this.avatar = user.avatar
        }
    }

    fun changePassword(oldPw: String, newPw: String) {
        userManager.hash = PasswordUtils.md5(newPw)
    }

    fun updateAvatar(url: String) {
        userManager.avatar = url
    }
}