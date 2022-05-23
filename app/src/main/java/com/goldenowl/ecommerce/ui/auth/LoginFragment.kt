package com.goldenowl.ecommerce.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.goldenowl.ecommerce.databinding.FragmentLoginBinding
import com.goldenowl.ecommerce.launchHome
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {
    private val REQ_ONE_TAP: Int = 100
    private val TAG = "LoginFragment"
    private lateinit var binding: FragmentLoginBinding


    private lateinit var auth: FirebaseAuth

    private lateinit var oneTapClient: SignInClient
    private var callbackManager: CallbackManager? = null

    //
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private val viewModel: AuthViewModel by activityViewModels()
    private val SIGN_IN_RESULT_CODE = 1001

    // 3
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)

        setViews(requireActivity())
        observeViews()

        setupListeners()

        auth = Firebase.auth

        return binding.root
    }

    private fun observeViews() {
        with(binding) {
            with(viewModel) {

                errorEmail.observe(viewLifecycleOwner) { errorEmail ->
                    if (errorEmail != null) {
                        inputLayoutEmail.error = errorEmail
                    } else {
                        inputLayoutEmail.isErrorEnabled = false
                        inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        Log.d(TAG, "observeViews: email valid")
                    }
                }

                errorPassword.observe(viewLifecycleOwner) { errorPassword ->
                    if (errorPassword != null) {
                        inputLayoutPassword.error = errorPassword
                        inputLayoutPassword.errorIconDrawable = null
                    } else {
                        inputLayoutPassword.isErrorEnabled = false
                        Log.d(TAG, "observeViews: password valid")
                    }
                }

                logInValid.observe(viewLifecycleOwner) { logInValid ->
                    Log.d(TAG, "observeViews: logInValid=$logInValid ")
                    btnLogin.isEnabled = logInValid
                }
            }

        }

    }

    private fun setupListeners() {
        with(binding) {
            edtEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    viewModel.checkEmail(edtEmail.text.toString())
                    viewModel.setLoginValid()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    viewModel.checkPassword(edtPassword.text.toString())
                    viewModel.setLoginValid()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }

    private fun setViews(context: Context) {
        with(binding) {

            btnLogin.setOnClickListener {
                Log.d(TAG, "setViews: begin sign in")
                logInWithEmail(context)
            }

            layoutForgotPassword.setOnClickListener {
                forgotPassword()
            }

            ivFacebook.setOnClickListener { logInWithFacebook() }
            ivGoogle.setOnClickListener { logInWithGoogle() }
        }
    }


    private fun logInWithGoogle3() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
            //
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    private fun logInWithGoogle() {
        Log.d(TAG, "logInGoogle: Log in google")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

//        firebaseAuth = FirebaseAuth.getInstance()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            Log.d(TAG, "logInWithGoogle: account: ${account.displayName} ${account.email}")
        } else {
            Log.d(TAG, "logInWithGoogle: account NULL")
        }

        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            RC_SIGN_IN
        )
    }


    private fun logInWithFacebook() {
        binding.ivFacebook.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "logInFacebook: logging in facebook")

            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile", "email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d(TAG, "Facebook token: " + loginResult.accessToken.token)
                        Log.d(TAG, "Facebook id: " + loginResult.accessToken.userId)
//                        startActivity(Intent(requireActivity(), AuthenticatedActivity::class.java))

                        handleFacebookAccessToken(loginResult.accessToken)

                        val sessionManager = SessionManager(requireActivity())
                        sessionManager.createLoginSession(
                            "1",
                            loginResult.accessToken.userId,
                            loginResult.accessToken.token
                        )
                        launchHome(requireActivity())
                    }

                    override fun onCancel() {
                        Log.d("MainActivity", "Facebook onCancel.")

                    }

                    override fun onError(error: FacebookException) {
                        Log.d("MainActivity", "Facebook onError.")

                    }
                })
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    private fun forgotPassword() {
        Log.d(TAG, "forgotPassword")
    }

    private fun logInWithEmail(context: Context) {
        val (email, password) = arrayOf(
            binding.edtEmail.text.toString(),
            binding.edtPassword.text.toString()
        )

        Log.d(TAG, "signInWithEmailPassword: $email : $password")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser

                    val sessionManager = SessionManager(context)
                    sessionManager.createLoginSession("1", email, password)
                    launchHome(context)

//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed: " + task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
//                    updateUI(null)
                }
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d(TAG, "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d(TAG, "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                }
            }
            Req_Code -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleResult(task)

            }
            SIGN_IN_RESULT_CODE -> {
                val response = IdpResponse.fromResultIntent(data)
                if (resultCode == Activity.RESULT_OK) {
                    // User successfully signed in.
                    Log.i(
                        TAG,
                        "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                    )
                } else {
                    // Sign in failed. If response is null, the user canceled the
                    // sign-in flow using the back button. Otherwise, check
                    // the error code and handle the error.
                    Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                }
            }
            RC_SIGN_IN -> {
                GoogleSignIn.getSignedInAccountFromIntent(data);
            }
        }

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "handleResult: ${account.displayName}")
//                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "handleResult: ${e.toString()}")
        }
    }
}