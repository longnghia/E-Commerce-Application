package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEEMAIL
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEFACEBOOK
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.ChangePasswordStatus
import com.goldenowl.ecommerce.utils.LoginStatus
import com.goldenowl.ecommerce.utils.PasswordUtils.md5
import com.goldenowl.ecommerce.utils.SignupStatus
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private fun showToastErr(s: String?) {
        Toast.makeText(getApplication<MyApplication>().applicationContext, s, Toast.LENGTH_SHORT).show()
    }


    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userManager: UserManager = UserManager.getInstance(application)
    private val db = Firebase.firestore
    private var currentUser = firebaseAuth.currentUser

    private val userRef = db.collection("users")
    private val imagesRef = Firebase.storage.reference


    val facebookCallbackManager = CallbackManager.Factory.create() //facebook callback

    var signUpStatus: MutableLiveData<SignupStatus> = MutableLiveData<SignupStatus>()
    var logInStatus: MutableLiveData<LoginStatus> = MutableLiveData<LoginStatus>()
    var changePasswordStatus: MutableLiveData<ChangePasswordStatus> = MutableLiveData<ChangePasswordStatus>()
    var forgotPasswordStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()

    var errorMessage: MutableLiveData<String> = MutableLiveData<String>()

    val mGoogleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(application.getString(R.string.web_client_id))
        .requestEmail()
        .build()

    val googleCallbackManager = object : ICallback {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != GOOGLE_SIGN_IN) {
                Log.d(TAG, "onActivityResult: requestCode = $requestCode")
                return
            }
            val googleAccount = GoogleSignIn.getSignedInAccountFromIntent(data).result
            if (googleAccount == null)
                showToastErr("Google SignIn Failed!")
            googleAccount?.let {
                Log.d(
                    TAG,
                    "onCreateView: Google sign-in completed with ${it.email} and token ${it.idToken}"
                )
                if (it.email == null || it.idToken == null) {
                    Log.w(TAG, "goolgeActivityResultLauncher: email or idtoken null")
                } else {
                    val firebaseCredential = GoogleAuthProvider.getCredential(it.idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            currentUser = firebaseAuth.currentUser
                            onLoginSuccess(currentUser, UserManager.TYPEGOOGLE)
                        }
                }
            }

            if (googleAccount == null) {
                Log.e(TAG, "onCreateView: googleAccount null")
            }
        }
    }


    init {
        signUpStatus.value = SignupStatus.NONE
        logInStatus.value = LoginStatus.NONE
        changePasswordStatus.value = ChangePasswordStatus.NONE
        forgotPasswordStatus.value = BaseLoadingStatus.NONE
        mGoogleSignInClient = GoogleSignIn.getClient(application, gso)
    }


    fun signUpWithEmail(email: String, password: String, name: String) {
        Log.d(TAG, "signUpMailPassword: sign up with email and password")

        signUpStatus.value = SignupStatus.LOADING
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    errorMessage.value = ""
                    android.util.Log.d(TAG, "createUserWithEmail:success")
                    currentUser = firebaseAuth.currentUser

                    if (currentUser != null) {
                        val user: User = User(
                            name = name,
                            email = currentUser!!.email,
                            dob = null,
                            password = md5(password),
                            avatar = currentUser?.photoUrl.let { it.toString() },
                            logType = TYPEEMAIL
                        )
                        Log.d(TAG, "onLoginSuccess: user = $user")
                        onSignUpSuccess(user)
                    }

                } else {
                    Log.e(TAG, "signUpWithEmail: failed", task.exception)
                    val errorCode = (task.exception as FirebaseAuthException).errorCode
                    task.exception?.message?.let { showToastErr(it) }

                    signUpStatus.value = SignupStatus.FAIL
                    val message = task.exception?.message
                        ?: getApplication<MyApplication>().getString(R.string.error_login_default_error)
                    errorMessage.value = message
                    showToastErr(message)
                }
            }
    }

    fun logOutFacebook() {
        if (AccessToken.getCurrentAccessToken() != null) {
            Log.d(TAG, "logging out facebook")
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()

                }).executeAsync()
        }
    }


    fun logInWithFacebook(fragment: Fragment) {
        Log.d(TAG, "logInWithFacebook: logging in with facebook")
        LoginManager.getInstance()
            .logInWithReadPermissions(fragment, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "Facebook token: " + loginResult.accessToken.token)
                    Log.d(TAG, "Facebook id: " + loginResult.accessToken.userId)
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d("MainActivity", "Facebook onCancel.")

                }

                override fun onError(error: FacebookException) {
                    Log.d("MainActivity", "Facebook onError.")

                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    currentUser = firebaseAuth.currentUser
                    onLoginSuccess(currentUser, TYPEFACEBOOK)

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    task.exception?.message?.let { showToastErr(it) }
                    logInStatus.value = LoginStatus.FAIL
                }
            }
    }

    private fun onLoginSuccess(currentUser: FirebaseUser?, logType: String?) {
        if (currentUser == null)
            Log.d(TAG, "onLoginSuccess: firebaseAuth.currentUser NULL ")
        else {
            val user: User = User(
                id = currentUser.uid,
                name = currentUser.displayName,
                email = currentUser.email,
                dob = null,
                password = null,
                avatar = currentUser?.photoUrl.let { it.toString() },
                logType = logType
            )
            Log.d(TAG, "onLoginSuccess: user = $user")
            addUserToFireStore(user)
            logInStatus.value = LoginStatus.SUCCESS
        }
        logCurrentUser(currentUser)
    }

    private fun logCurrentUser(currentUser: FirebaseUser?) {
        currentUser?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid

                // Name, email address, and profile photo Url
                val name = profile.displayName
                val email = profile.email
                val photoUrl = profile.photoUrl

                Log.d(TAG, "logCurrentUser: provider=$providerId uid=$uid name=$name email=$email photo=$photoUrl")
            }
        }
    }

    private fun addUserToFireStore(user: User) {
        val cUserRef = user.id?.let { userRef.document(it) }
        cUserRef?.get()?.addOnCompleteListener {
            if (it.isSuccessful) {
                val doc = it.result
                if (!doc.exists()) {
                    Log.d(TAG, "addUserToFireStore: saving new user")
                    cUserRef.set(user).addOnCompleteListener {
                        Log.d(TAG, "addUserToFireStore: add new user successfully")
                    }
                    userManager.addAccount(user)
                } else {
                    Log.d(TAG, "addUserToFireStore: old user: ${user.name}")
                    Log.d(TAG, "addUserToFireStore: ${doc.data}")
                    userManager.addAccount(user)
                }
            }
        }
    }

    fun logInWithEmail(email: String, password: String) {

        Log.d(TAG, "signInWithEmailPassword: $email : $password")
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val currentUser = firebaseAuth.currentUser
                    onLoginSuccess(currentUser, TYPEEMAIL)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    logInStatus.value = LoginStatus.FAIL
                    errorMessage.value = task.exception?.message
                }
            }
    }

    private fun onSignUpSuccess(user: User) {
        if (currentUser == null)
            Log.d(TAG, "onSignUpSuccess: firebaseAuth.currentUser NULL ")
        else {
            userManager.addAccount(user)
            addUserToFireStore(user)
            signUpStatus.value = SignupStatus.SUCCESS
        }
    }

    fun forgotPassword(email: String) {
        Log.d(TAG, "forgotPassword clicked")
        forgotPasswordStatus.value = BaseLoadingStatus.LOADING

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    forgotPasswordStatus.value = BaseLoadingStatus.SUCCESS
                    val msg =
                        getApplication<MyApplication>().getString(R.string.email_rs_password_sent) + " " + email
                    showToastErr(msg)
                } else {
                    Log.e(TAG, "forgotPassword: error:", task.exception)
                    val msg = task.exception?.message
                    showToastErr(msg)
                    forgotPasswordStatus.value = BaseLoadingStatus.FAILURE
                }
            }

    }

    fun logInWithGoogle(fragment: Fragment) {
        Log.d(TAG, "logInGoogle: Log in google")
        val signInIntent = mGoogleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)

//        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(fragment.requireContext());
//        if (account != null) {
//            Log.d(TAG, "logInWithGoogle: account: ${account.displayName} ${account.email}")
//            Log.d(TAG, "logInWithGoogle: logging out")
//            Firebase.auth.signOut()
//            mGoogleSignInClient.signOut()
//                .addOnCompleteListener(requireActivity(), OnCompleteListener<Void?> {
//                    Log.d(TAG, "logInWithGoogle: logged out")
//                })
//        } else {
//            Log.d(TAG, "logInWithGoogle: start signing intent")
//            val signInIntent = mGoogleSignInClient.signInIntent
//            goolgeActivityResultLauncher.launch(signInIntent)
//        }

    }

    fun changePassword(oldPassword: String, newPassword: String) {
        val credential = EmailAuthProvider
            .getCredential(userManager.email, oldPassword)
        currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener {
                Log.d(TAG, "User re-authenticated.")
                currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "savePassword: successful")
                        userManager.access_token = md5(newPassword)
                        updateFirestore()
                        changePasswordStatus.value = ChangePasswordStatus.SUCCESS
                    } else {
                        Log.d(TAG, "savePassword: fail ${task.exception}")
                    }
                }
            }

    }

    private fun updateFirestore() {
        val user = userManager.getUser()
        user.id?.let {
            userRef.document(it).set(user).addOnCompleteListener {
                Log.d(TAG, "updateFirestore: successful")
            }
        }
    }

    fun updateAvatar(file: Uri?) {
        val avatarRef = imagesRef.child("images/${file?.lastPathSegment}")
        var uploadTask = file?.let { imagesRef.putFile(it) }

        uploadTask?.addOnFailureListener {
            showToastErr(it.message)
        }?.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "init: successfull ${taskSnapshot.metadata?.path}")
            taskSnapshot.metadata?.path?.let {
                avatarRef.child(it).downloadUrl.addOnSuccessListener {
                    // Got the download URL for 'users/me/profile.png'
                    Log.d(TAG, "init: downloadurl = ${it}")
                }.addOnFailureListener {
                    // Handle any errors
                }
            }
        }
    }

    fun emptyErrorMessage() {
        errorMessage.value = ""
    }


    companion object {
        const val TAG = "AuthViewModel"
        const val GOOGLE_SIGN_IN = 100
    }

}

interface ICallback {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}