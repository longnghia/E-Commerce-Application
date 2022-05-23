package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.TextValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    var currentUser: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()

    var errorName: MutableLiveData<String> = MutableLiveData<String>()
    var errorEmail: MutableLiveData<String> = MutableLiveData<String>()
    var errorPassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorOldPassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorRePassword: MutableLiveData<String> = MutableLiveData<String>()
    var errorDoB: MutableLiveData<String> = MutableLiveData<String>()


    //    var infoValid: MutableLiveData<Boolean>  = MutableLiveData<Boolean>()
    var logInValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var changePasswordValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var signUpValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        logInValid.value = false
        signUpValid.value = false
        changePasswordValid.value = false
    }

    fun setLoginValid() {
        logInValid.value = errorEmail.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty()
    }

    fun setSignUpValid() {
        signUpValid.value =
            errorName.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty() && errorEmail.value.isNullOrEmpty()
    }

    fun setChangePasswordValid() {
        changePasswordValid.value =
            errorOldPassword.value.isNullOrEmpty() && errorPassword.value.isNullOrEmpty() && errorRePassword.value.isNullOrEmpty()
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

    fun checkOldPassword(password: String, oldPassword:String) {
        val error = if (password == oldPassword) null else "Wrong password"
        errorOldPassword.value = error
    }

    fun checkDoB(dob: String) {
        val error = TextValidation.validateDoB(dob)
        errorDoB.value = error
    }

    fun checkRePassword(password:String, rePassword: String) {
        val error = if (password == rePassword) null else "Password mismatch"
        errorRePassword.value = error
    }

    fun signUpWithEmail(auth: FirebaseAuth, email: String, password: String) {
        Log.d(TAG, "signUpMailPassword: sign up with email and password")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    android.util.Log.d(TAG, "createUserWithEmail:success")
                    currentUser.value = auth.currentUser

//                        updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    android.util.Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    android.widget.Toast.makeText(
//                        context, "Authentication failed.",
//                        android.widget.Toast.LENGTH_SHORT
//                    ).show()
//
//                    signUpFailure()
//                        updateUI(null)
                }
            }


    }


    companion object {
        const val TAG = "AuthViewModel"
    }

}