package com.goldenowl.ecommerce.ui.auth

import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSignupBinding
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.LoginStatus
import com.goldenowl.ecommerce.utils.SignupStatus
import com.goldenowl.ecommerce.utils.launchHome
import com.google.android.material.textfield.TextInputLayout


class SignupFragment : BaseAuthFragment<FragmentSignupBinding>() {
    private val TAG = "SignupFragment"


    override fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.signup)
    }

    override fun setObservers() {

        textInputViewModel.errorEmail.observe(viewLifecycleOwner) { errorEmail ->
            validEmail(errorEmail)
        }
        textInputViewModel.errorPassword.observe(viewLifecycleOwner) { errorPassword ->
            validPassword(errorPassword)
        }

        textInputViewModel.signUpFormValid.observe(viewLifecycleOwner) { signUpValid ->
            Log.d(TAG, "setObservers: logInValid=$signUpValid ")
            binding.btnSignup.isEnabled = signUpValid
        }

        viewModel.signUpStatus.observe(viewLifecycleOwner) {
            handelSignUp(it)
        }

        viewModel.logInStatus.observe(viewLifecycleOwner) {
            handleLogin(it)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            hasError(it)
        }
    }

    private fun handleLogin(it: LoginStatus?) {
        Log.d(TAG, "validSignup: $it")
        when (it) {
            LoginStatus.LOADING -> {
                setLoading(true)
                binding.btnSignup.isEnabled = false
            }
            LoginStatus.SUCCESS -> {
                setLoading(false)
                launchHome(requireContext())
                activity?.finish()
            }
            LoginStatus.FAIL -> {
                setLoading(false)
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun hasError(it: String?) {
        if (it != null) {
            if (it.isNotEmpty()) {
                binding.inputLayoutEmail.error = it
                Log.d(TAG, "hasError: $it")
            }
        }
    }

    private fun handelSignUp(signUpStatus: SignupStatus) {
        Log.d(TAG, "validSignup: $signUpStatus")
        when (signUpStatus) {
            SignupStatus.LOADING -> {
                setLoading(true)
                binding.btnSignup.isEnabled = false
            }
            SignupStatus.SUCCESS -> {
                setLoading(false)
                launchHome(requireContext())
                activity?.finish()
            }
            SignupStatus.FAIL -> {
                setLoading(false)
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun validPassword(errorPassword: String?) {
        with(binding) {
            if (errorPassword != null) {
                inputLayoutPassword.error = errorPassword
                inputLayoutPassword.errorIconDrawable = null
            } else {
                inputLayoutPassword.isErrorEnabled = false
                Log.d(TAG, "setObservers: password valid")
            }
        }
    }

    private fun validEmail(errorEmail: String?) {
        with(binding) {
            if (errorEmail != null) {
                inputLayoutEmail.error = errorEmail
            } else {
                inputLayoutEmail.isErrorEnabled = false
                inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
                Log.d(TAG, "setObservers: email valid")
            }
        }
    }

    override fun setupListeners() {
        with(binding) {
            edtEmail.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkEmail(edtEmail.text.toString())
                    textInputViewModel.setSignUpFormValid()
                }

            })

            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString())
                    textInputViewModel.setSignUpFormValid()
                }
            })

            edtName.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkName(edtName.text.toString())
                    textInputViewModel.setSignUpFormValid()
                }
            })

        }
    }

    override fun setViews() {
        with(binding) {
            btnSignup.setOnClickListener {
                Log.d(TAG, "setViews: begin sign up")
                viewModel.signUpWithEmail(
                    edtEmail.text.toString(),
                    edtPassword.text.toString(),
                    edtName.text.toString()
                )
            }

            layoutAlreadyHasAcc.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.login_dest)
            )
            ivFacebook.setOnClickListener { viewModel.logInWithFacebook(this@SignupFragment) }
            ivGoogle.setOnClickListener { viewModel.logInWithGoogle(this@SignupFragment) }
        }

    }

    private fun setLoading(isShow: Boolean) {
        if (isShow) {
            binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
            binding.layoutLoading.circularLoader.showAnimationBehavior
        } else {
            binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
        }
    }

    override fun getViewBinding(): FragmentSignupBinding {
        return FragmentSignupBinding.inflate(layoutInflater)
    }
}