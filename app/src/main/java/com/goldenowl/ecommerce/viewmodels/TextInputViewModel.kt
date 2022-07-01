package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.utils.PasswordUtils.md5
import com.goldenowl.ecommerce.utils.TextValidation

class TextInputViewModel(application: Application) : AndroidViewModel(application) {

    var errorName: MutableLiveData<String> = MutableLiveData<String>()
    var errorLoginEmail: MutableLiveData<String> = MutableLiveData<String>()
    var errorLoginPassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorSignUpEmail: MutableLiveData<String> = MutableLiveData<String>()
    var errorSignUpPassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorOldPassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorRePassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorDoB: MutableLiveData<String> = MutableLiveData<String>()

    var logInFormValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var changePasswordValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var signUpFormValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        logInFormValid.value = false
        signUpFormValid.value = false
        changePasswordValid.value = false
    }

    fun setLoginFormValid() {
        logInFormValid.value =
            errorLoginEmail.value.isNullOrEmpty() && errorLoginPassword.value.isNullOrEmpty()
    }

    fun setSignUpFormValid() {
        signUpFormValid.value =
            errorName.value.isNullOrEmpty() && errorSignUpEmail.value.isNullOrEmpty() && errorSignUpPassword.value.isNullOrEmpty()
    }

    fun setChangePasswordValid() {
        changePasswordValid.value =
            errorOldPassword.value.isNullOrEmpty() && errorLoginPassword.value.isNullOrEmpty() && errorRePassword.value.isNullOrEmpty()
        changePasswordValid.value = true // todo delete
    }

    fun checkName(name: String) {
        val error = TextValidation.validateName(name)
        errorName.value = error
    }

    fun checkEmail(email: String, type: Int) {
        val error = TextValidation.validateEmail(email)
        if (type == 0)
            errorLoginEmail.value = error
        if (type == 1)
            errorSignUpEmail.value = error
    }

    /**
     * check if password input valid
     * @param type =0 if login, =1 if signup
     */
    fun checkPassword(password: String, type: Int) {
        val error = TextValidation.validatePassword(password.trim())
        if (type == 0) errorLoginPassword.value = error
        if (type == 1) errorSignUpPassword.value = error

    }

    fun checkOldPassword(password: String, oldPasswordHash: String) {
        val error = if (md5(password) == oldPasswordHash) "" else "Wrong password"
        errorOldPassword.value = error
        Log.d(TAG, "checkOldPassword: $oldPasswordHash, ${md5(password)} $password ")
    }

    fun checkDoB(dob: String) {
        val error = TextValidation.validateDoB(dob.trim())
        errorDoB.value = error
    }

    fun checkRePassword(password: String, rePassword: String) {
        val error = if (password == rePassword) "" else "Password mismatch"
        errorRePassword.value = error
    }


    companion object {
        const val TAG = "TextInputViewModel"
    }
}