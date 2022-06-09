package com.goldenowl.ecommerce.models.repo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEFACEBOOK
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.MyResult
import com.goldenowl.ecommerce.utils.PasswordUtils
import com.goldenowl.ecommerce.utils.PasswordUtils.md5
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class RemoteAuthDataSource(private val userManager: UserManager, val context: Context) : AuthDataSource {
    private val dispatchers: CoroutineContext = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser = firebaseAuth.currentUser

    private val userRef = db.collection("users")
    private val storageRef = Firebase.storage.reference
    private val settingsManager = SettingsManager(context)

    override fun isLogin(): Boolean {
        Log.d(TAG, "isLogin: currentUser = $currentUser")
        return currentUser != null
    }

    override fun logOut() {
        when (userManager.logType) {
            UserManager.TYPEFACEBOOK -> {
                logOutFacebook()
            }
            else -> {
                if (currentUser != null)
                    firebaseAuth.signOut()
                else {
                    Log.d(AuthRepository.TAG, "logOut: current user: ${firebaseAuth.currentUser}")
                }

            }
        }
    }

    override fun getUserId(): String? {
        return currentUser?.uid
    }

    suspend fun updateUserData(fullName: String, dob: String, settings: Map<String, Boolean>): String? {
        return withContext(Dispatchers.IO) {
            try {
                val currentUserRef = userRef.document(getUserId()!!)
                val userDataSnapshot = currentUserRef.get().await()
                if (userDataSnapshot.exists()) {
                    val user = userDataSnapshot.toObject(User::class.java)
                    if (user != null) {
                        user.name = fullName
                        user.dob = dob
                        user.settings = settings
                        currentUserRef.set(user)
                        Log.d(TAG, "updateUserData: successfully")
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "updateUserData: ERROR", e)
                return@withContext e.message
            }
        }
    }

    val facebookCallbackManager = CallbackManager.Factory.create() //facebook callback

    val mGoogleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.web_client_id))
        .requestEmail()
        .build()

    val googleCallbackManager = object : ICallback {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, listener: LoginListener) {
            if (requestCode != GOOGLE_SIGN_IN) {
                Log.d(TAG, "onActivityResult: requestCode = $requestCode")
                return
            }
            val googleAccount = GoogleSignIn.getSignedInAccountFromIntent(data).result
            googleAccount?.let {
                Log.d(
                    TAG,
                    "onCreateView: Google sign-in completed with ${it.email} and token ${it.idToken}"
                )
                if (it.email == null || it.idToken == null) {
                    Log.w(TAG, "goolgeActivityResultLauncher: email or idtoken null")
                    listener.callback(MyResult.Error(java.lang.Exception("User not found")))
                } else {
                    val firebaseCredential = GoogleAuthProvider.getCredential(it.idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener {
                            Log.d(TAG, "onActivityResult: signIn Google WithCredential success ")
                            currentUser = firebaseAuth.currentUser
                            listener.callback(MyResult.Success(true))
                            onLoginSuccess(currentUser, UserManager.TYPEGOOGLE)

                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onActivityResult: ERROR", e)
                            listener.callback(MyResult.Error(e))
                        }
                }
            }

            if (googleAccount == null) {
                Log.e(TAG, "onCreateView: googleAccount null")
            }
        }
    }

    init {
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }


    suspend fun signUpWithEmail(email: String, password: String, name: String): String? {
        return withContext(dispatchers) {
            Log.d(TAG, "signUpMailPassword: sign up with email and password")

            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val user = User(
                        name = name,
                        email = currentUser!!.email!!,
                        dob = "",
                        password = PasswordUtils.md5(password),
                        avatar = currentUser?.photoUrl.toString(),
                        logType = UserManager.TYPEEMAIL
                    )
                    Log.d(TAG, "signUpMailPassword: user = $user")
                    onSignUpSuccess(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "signUpWithEmail: ERROR", e)
                return@withContext e.message
            }
            return@withContext null
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


    fun logInWithFacebook(fragment: Fragment, listener: LoginListener) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        Log.d(TAG, "logInWithFacebook: isloggedIn=$isLoggedIn")
        Log.d(TAG, "logInWithFacebook: logging in with facebook")
        LoginManager.getInstance()
            .logInWithReadPermissions(fragment, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "Facebook token: " + loginResult.accessToken.token)
                    Log.d(TAG, "Facebook id: " + loginResult.accessToken.userId)
                    handleFacebookAccessToken(loginResult.accessToken, listener)
                    listener.callback(MyResult.Success(true))
                }

                override fun onCancel() {
                    Log.d("MainActivity", "Facebook onCancel.")

                }

                override fun onError(error: FacebookException) {
                    Log.d("MainActivity", "Facebook onError.")
                    listener.callback(MyResult.Error(error))
                }
            })
    }


    private fun handleFacebookAccessToken(token: AccessToken, listener: LoginListener) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    currentUser = firebaseAuth.currentUser
                    listener.callback(MyResult.Success(true))
                    onLoginSuccess(currentUser, TYPEFACEBOOK)

                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    listener.callback(MyResult.Error(java.lang.Exception(task.exception)))
                }
            }
    }

    private fun onLoginSuccess(currentUser: FirebaseUser?, logType: String) {
        if (currentUser == null)
            Log.d(TAG, "onLoginSuccess: firebaseAuth.currentUser NULL ")
        else {
            val user = User(
                id = currentUser.uid,
                name = currentUser.displayName ?: "",
                email = currentUser.email ?: "",
                dob = "",
                password = "",
                avatar = currentUser?.photoUrl.let { it.toString() },
                logType = logType
            )
            Log.d(TAG, "onLoginSuccess: user = $user")
            addUserToFireStore(user)
        }
        logCurrentUser(currentUser)
    }

    private fun restoreUserData() {
        currentUser = firebaseAuth.currentUser
        userRef.document(currentUser!!.uid).get().addOnCompleteListener {
            val userDataSnapshot = it.result
            if (!userDataSnapshot.exists()) {
                Log.d(TAG, "restoreUser: user ${currentUser!!.uid} not exist")
            } else {
                val user = userDataSnapshot.toObject(User::class.java)
                Log.d(TAG, "restoreUser: $user")
                userManager.addAccount(user!!)
                settingsManager.saveUserSettings(user.settings)
            }
        }.addOnFailureListener {
            Log.e(TAG, "restoreUserData: ERROR", it)
        }
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
        val cUserRef = user.id.let { userRef.document(it) }
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
                    Log.d(TAG, "addUserToFireStore: user existed: ${doc.data}")
                    userManager.addAccount(user)
                    restoreUserData()
                }
            } else {
                Log.w(TAG, "addUserToFireStore: ERROR", it.exception)
            }
        }
    }

    suspend fun logInWithEmail(email: String, password: String): String? {
        Log.d(TAG, "signInWithEmailPassword: $email : $password")
        return withContext(dispatchers) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val currentUser = firebaseAuth.currentUser
                onLoginSuccess(currentUser, UserManager.TYPEEMAIL)
            } catch (e: Exception) {
                Log.e(TAG, "logInWithEmail: ERROR", e)
                return@withContext e.message
            }
            return@withContext null
        }

    }

    private fun onSignUpSuccess(user: User) {
        if (currentUser == null)
            Log.d(TAG, "onSignUpSuccess: firebaseAuth.currentUser NULL ")
        else {
            userManager.addAccount(user)
            addUserToFireStore(user)
        }
    }

    suspend fun forgotPassword(email: String): String? {
        Log.d(TAG, "forgotPassword clicked")

        return withContext(dispatchers) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "forgotPassword: error:", e)
                val msg = e?.message
                return@withContext msg
            }
            return@withContext null
        }
    }

    fun logInWithGoogle(fragment: Fragment) {
        Log.d(TAG, "logInGoogle: Log in google")
        val signInIntent = mGoogleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): String? {
        return withContext(dispatchers) {
            try {
                val credential = EmailAuthProvider
                    .getCredential(userManager.email, oldPassword)
                currentUser?.reauthenticate(credential)?.await()
                Log.d(TAG, "User reauthenticated.")
                currentUser?.updatePassword(newPassword)?.await()
                Log.d(TAG, "savePassword: successfully")
                userManager.hash = md5(newPassword)
                updateFirestore()

                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "changePassword: ERROR", e)
                return@withContext e.message
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

    suspend fun updateAvatar(userId: String, file: Uri?): String? {
        return withContext(dispatchers) {
            try {
                val avatarRef = storageRef.child("images/$userId/${file?.lastPathSegment}")
                var task = file?.let { avatarRef.putFile(it) }?.await()
                Log.d(TAG, "init: upload successfull ${task?.metadata?.path}")

                // todo update user info user ref
//                avatarRef.child(it).downloadUrl


                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "updateAvatar: ERROR", e)
                return@withContext e.message
            }
        }
    }


    companion object {
        const val TAG = "RemoteAuthDataSource"
        const val GOOGLE_SIGN_IN = 100
    }
}

interface LoginListener {
    fun callback(result: MyResult<Boolean>)
}

interface ICallback {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, listener: LoginListener)
}
