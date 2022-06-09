package com.goldenowl.ecommerce.models.repo

import android.net.Uri
import androidx.fragment.app.Fragment

class AuthRepository(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val localAuthDataSource: LocalAuthDataSource
) {

    fun isUserLoggedIn(): Boolean {
        return localAuthDataSource.isLogin()
    }


    fun getUserId(): String {
        return localAuthDataSource.getUserId()
    }

    fun logOut() {
        remoteAuthDataSource.logOut()
        localAuthDataSource.logOut()
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): String? {
        return remoteAuthDataSource.signUpWithEmail(email, password, name)
    }

    fun logInWithFacebook(fragment: Fragment, listener: LoginListener) {
        remoteAuthDataSource.logInWithFacebook(fragment, listener)
    }

    suspend fun logInWithEmail(email: String, password: String): String? {
        return remoteAuthDataSource.logInWithEmail(email, password)
    }

    suspend fun forgotPassword(email: String): String? {
        return remoteAuthDataSource.forgotPassword(email)
    }

    fun logInWithGoogle(fragment: Fragment) {
        remoteAuthDataSource.logInWithGoogle(fragment)
    }

    suspend fun changePassword(oldPw: String, newPw: String): String? {
        val res = remoteAuthDataSource.changePassword(oldPw, newPw)
        if (res.isNullOrEmpty())
            localAuthDataSource.changePassword(oldPw, newPw)
        return res
    }

    fun callBackManager(): ICallback {
        return remoteAuthDataSource.googleCallbackManager
    }

    suspend fun updateAvatar(userId: String, file: Uri?): String? {
        val err = remoteAuthDataSource.updateAvatar(userId, file)
        if (err.isNullOrEmpty()) localAuthDataSource.updateAvatar(file)
        return err
    }

    suspend fun updateUserData(fullName: String, dob: String, settings: Map<String, Boolean>): String? {
        return remoteAuthDataSource.updateUserData(fullName, dob, settings).apply {
            localAuthDataSource.updateUserData(fullName, dob)
        }
    }

    companion object {
        val TAG = "AuthRepository"
    }
}
