package com.goldenowl.ecommerce.models.repo

import com.google.firebase.auth.FirebaseAuth

interface AuthRepoInterface {
    var firebaseAuth: FirebaseAuth

    fun logInWithEmail()
    fun logInWithGoogle()
    fun logInWithFacebook()

    fun signInWithEmail()
    fun signInWithGoogle()
    fun signInWithFacebook()

}
