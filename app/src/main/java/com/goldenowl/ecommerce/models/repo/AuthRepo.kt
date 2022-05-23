//package com.goldenowl.ecommerce.models.repo
//
//import android.app.Activity
//import android.util.Log
//import android.widget.Toast
//import com.goldenowl.ecommerce.models.data.UserData
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//
//class AuthRepo (val userData: UserData){
//    val TAG="AuthRepo"
//
//    var auth: FirebaseAuth = Firebase.auth
//
//    fun logInWithEmail( email: String, password:String, activity: Activity){
//
//
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(activity) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
//                    signUpSuccess()
////                        updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        context, "Authentication failed.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    signUpFailure()
////                        updateUI(null)
//                }
//            }
//    }
//    fun logInWithGoogle()
//    fun logInWithFacebook()
//
//    fun signInWithEmail()
//    fun signInWithGoogle()
//    fun signInWithFacebook()
//
//}