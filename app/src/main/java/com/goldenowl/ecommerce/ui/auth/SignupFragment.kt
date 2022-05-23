package com.goldenowl.ecommerce.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.TutorialActivity
import com.goldenowl.ecommerce.databinding.FragmentSignupBinding
import com.goldenowl.ecommerce.launchHome
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignupFragment : Fragment() {

    private val TAG = "SignupFragment"
    private lateinit var binding: FragmentSignupBinding

    private lateinit var auth: FirebaseAuth

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentSignupBinding.inflate(layoutInflater)



        setupAuth()
        checkAuth()

        setViews()
        observeViews()
        setupListeners()


        return binding.root
    }



    private fun checkAuth() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "checkAuth: user logged in ${currentUser.email}, ${currentUser.uid}")
        }

    }

    private fun setupAuth() {
        auth = Firebase.auth

    }



    private fun signUpWithMail() {
        with(binding) {
            viewModel.signUpWithEmail(auth, edtEmail.text.toString(), edtPassword.text.toString())
        }
    }

    private fun signUpSuccess() {
        val sessionManager = SessionManager(requireActivity())
        sessionManager.createLoginSession(
            "123",
            binding.edtEmail.text.toString(),
            binding.edtPassword.text.toString()
        )

            launchHome(requireActivity())
    }

    private fun signUpFailure() {
        Log.e(TAG, "signUpFailure: fail to sign up")
    }

    private fun signUpWithFacebook() {
        Log.d(TAG, "signUpFacebook: sign up facebook")
    }

    private fun signUpWithGoogle() {
        Log.d(TAG, "signUpGoogle: sign up google")
//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.your_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            .build()
    }



    private fun observeViews() {
        with(binding) {

            viewModel.errorEmail.observe(viewLifecycleOwner) { errorEmail ->
                if (errorEmail != null) {
                    inputLayoutEmail.error = errorEmail
                } else {
                    inputLayoutEmail.isErrorEnabled = false
                    inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    Log.d(TAG, "observeViews: email valid")
                }
            }

            viewModel.errorPassword.observe(viewLifecycleOwner) { errorPassword ->
                if (errorPassword != null) {
                    inputLayoutPassword.error = errorPassword
                    inputLayoutPassword.errorIconDrawable = null
                } else {
                    inputLayoutPassword.isErrorEnabled = false
                    Log.d(TAG, "observeViews: password valid")
                }
            }

            viewModel.signUpValid.observe(viewLifecycleOwner) { signUpValid ->
                Log.d(TAG, "observeViews: logInValid=$signUpValid ")
                btnSignup.isEnabled = signUpValid
            }

            viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    Log.d(TAG, "onCreateView: viewmodel.currentuser changed: $user ${user.email} $user.uid")
                    signUpSuccess()
                } else {
                    Log.d(TAG, "onCreateView:  viewmodel.currentuser  null")
                }
            }
        }

    }

    private fun setupListeners() {
        with(binding) {
            edtEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    viewModel.checkEmail(edtEmail.text.toString())
                    viewModel.setSignUpValid()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    viewModel.checkPassword(edtPassword.text.toString())
                    viewModel.setSignUpValid()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    viewModel.checkName(edtName.text.toString())
                    viewModel.setSignUpValid()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

        }
    }

    private fun setViews() {
        with(binding) {
            btnSignup.setOnClickListener {
                Log.d(TAG, "setViews: begin sign up")
                signUpWithMail()
            }

            layoutAlreadyHasAcc.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.login_dest)
            )

            ivFacebook.setOnClickListener { signUpWithFacebook() }
            ivGoogle.setOnClickListener { signUpWithGoogle() }
        }

    }
}