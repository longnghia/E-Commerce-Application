package com.goldenowl.ecommerce.models.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.goldenowl.ecommerce.models.data.User

class UserManager(context: Context) {
    private val accountManager: AccountManager = AccountManager.get(context)

    var dob: String
        get() {
            return accountManager.getUserData(getAccount(), DOB) ?: ""
        }
        set(value) {
            Log.d("setdob", "value: $value")
            accountManager.setUserData(getAccount(), DOB, value)
        }
    var logType: String
        get() {
            return accountManager.getUserData(getAccount(), LOGTYPE) ?: TYPEEMAIL
        }
        set(value) {
            accountManager.setUserData(getAccount(), LOGTYPE, value)
        }
    var name: String
        get() {
            return accountManager.getUserData(getAccount(), NAME) ?: ""
        }
        set(value) {
            accountManager.setUserData(getAccount(), NAME, value)
        }
    var email: String
        get() {
            return accountManager.getUserData(getAccount(), EMAIL) ?: ""
        }
        set(value) {
            accountManager.setUserData(getAccount(), EMAIL, value)
        }
    var avatar: String
        get() {
            return accountManager.getUserData(getAccount(), AVATAR) ?: ""
        }
        set(value) {
            accountManager.setUserData(getAccount(), AVATAR, value)
        }

    var hash: String
        get() {
            return accountManager.getUserData(getAccount(), HASH_PASSWORD) ?: ""
        }
        set(value) {
            accountManager.setUserData(getAccount(), HASH_PASSWORD, value)
        }

    var id: String
        get() {
            return accountManager.getUserData(getAccount(), ID) ?: ""
        }
        set(value) {
            accountManager.setUserData(getAccount(), ID, value)
        }

    fun addAccount(
        id: String,
        email: String,
        access_token: String,
        name: String,
        logType: String,
        avatar: String,
        dob: String

    ) {

        val data = Bundle()
            .apply {
                this.putString(ID, id)
                this.putString(HASH_PASSWORD, access_token)
                this.putString(EMAIL, email)
                this.putString(NAME, name)
                this.putString(LOGTYPE, logType)
                this.putString(AVATAR, avatar)
                this.putString(DOB, dob)
            }
        val account = Account(email, ACCOUNT_TYPE)
        accountManager.addAccountExplicitly(account, access_token, data)
        accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, access_token)
    }

    fun addAccount(
        user: User
    ) {
        user.apply {
            addAccount(id, email, password, name, logType, avatar, dob)
        }
    }

    private fun getAccount(): Account? {
        return if (accountManager.getAccountsByType(ACCOUNT_TYPE).isNotEmpty()) {
            accountManager.getAccountsByType(ACCOUNT_TYPE)[0]
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            return true
        }
        return false
    }

    fun logOut() {
        if (getAccount() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccountExplicitly(getAccount())
            }
        }
    }

    fun getUser(): User {
        return User(id, name, email, dob, hash, avatar, logType)
    }

    override fun toString(): String {
        return if (isLoggedIn())
            "UserManager(email='$email', avatar='$avatar', id='$id',dob=$dob, avatar=$avatar," +
                    "hash=$hash)"
        else "NULL"
    }

    companion object {
        const val AUTH_TOKEN_TYPE = "com.goldenowl.ecommerce"
        const val ACCOUNT_TYPE = "com.goldenowl.ecommerce"
        const val HASH_PASSWORD = "access_token"
        const val EMAIL = "email"
        const val ID = "id"
        const val NAME = "name"
        const val DOB = "dob"
        const val AVATAR = "avatar"
        const val LOGTYPE = "logtype"
        const val TYPEEMAIL = "typeEmail"
        const val TYPEGOOGLE = "typeGoogle"
        const val TYPEFACEBOOK = "typeFacebook"

        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context)
            }
        }
    }

}
