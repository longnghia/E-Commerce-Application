package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.utils.PasswordUtils.md5
import com.goldenowl.ecommerce.utils.TextValidation

class TextInputViewModel(application: Application) : AndroidViewModel(application) {

    var errorName: MutableLiveData<String> = MutableLiveData<String>()
    var errorEmail: MutableLiveData<String> = MutableLiveData<String>()
    var errorPassword: MutableLiveData<String> = MutableLiveData<String>()
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
            errorEmail.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty()
    }

    fun setSignUpFormValid() {
        signUpFormValid.value =
            errorName.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty() && errorEmail.value.isNullOrEmpty()
    }

    fun setChangePasswordValid() {
        changePasswordValid.value =
            errorOldPassword.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty() && errorRePassword.value.isNullOrEmpty()
        changePasswordValid.value = true // todo delete
    }

    fun checkName(name: String) {
        val error = TextValidation.validateName(name)
        errorName.value = error
    }

    fun checkEmail(email: String) {
        val error = TextValidation.validateEmail(email)
        errorEmail.value = error
    }

    fun checkPassword(password: String) {
        val error = TextValidation.validatePassword(password)
        errorPassword.value = error
    }

    fun checkOldPassword(password: String, oldPassword: String) {
        val error = if ( md5(password) == oldPassword) "" else "Wrong password"
        errorOldPassword.value = error
        Log.d(TAG, "checkOldPassword: $oldPassword, ${md5(password)} $password ")
    }

    fun checkDoB(dob: String) {
        val error = TextValidation.validateDoB(dob)
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