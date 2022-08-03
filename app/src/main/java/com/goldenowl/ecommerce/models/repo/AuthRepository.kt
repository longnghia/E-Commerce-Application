package com.goldenowl.ecommerce.models.repo

import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.models.repo.datasource.LocalAuthDataSource
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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

    fun logInWithFacebook(facebookCallbackManager: CallbackManager, listener: LoginListener) {
        remoteAuthDataSource.logInWithFacebook(facebookCallbackManager, listener)
    }

    fun logInWithEmail(email: String, password: String, listener: LoginListener){
        remoteAuthDataSource.logInWithEmail(email, password, listener)
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

    suspend fun updateAvatar(userId: String, file: File): MyResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = remoteAuthDataSource.uploadAvatar(userId, file)
                remoteAuthDataSource.updateAvatar(userId, url)
                localAuthDataSource.updateAvatar(url)
                MyResult.Success(url)
            } catch (e: Exception) {
                MyResult.Error(e)
            }
        }
    }


    suspend fun updateUserData(user: User): String? {
        localAuthDataSource.updateUserData(user)
        return remoteAuthDataSource.updateUserData(user)
    }

    suspend fun getUserById(userId: String): MyResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = remoteAuthDataSource.getUserById(userId)
                return@withContext MyResult.Success(user)
            } catch (e: Exception) {
                return@withContext MyResult.Error(e)
            }
        }
    }

    fun getUser(): User? {
        return localAuthDataSource.getUser()
    }

    fun restoreUserSettingsData(userId: String): MyResult<Boolean> {
        return try {
            remoteAuthDataSource.restoreUserData(userId)
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e)
        }
    }

    companion object {
        val TAG = "AuthRepository"
    }
}
