package com.goldenowl.ecommerce.ui.auth

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentLoginBinding
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.utils.Utils.launchHome
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : BaseAuthFragment<FragmentLoginBinding>() {
    private val TAG = "LoginFragment"

    override fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.login)
        binding.topAppBar.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun setObservers() {
        with(textInputViewModel) {
            errorLoginEmail.observe(viewLifecycleOwner) { errorEmail ->
                handleErrorEmail(errorEmail)
            }

            errorLoginPassword.observe(viewLifecycleOwner) { errorPassword ->
                handlePassword(errorPassword)
            }

            logInFormValid.observe(viewLifecycleOwner) { logInValid ->
                Log.d(TAG, "setObservers: logInValid=$logInValid ")
                binding.btnLogin.isEnabled = logInValid
            }
        }

        with(viewModel) {
            logInStatus.observe(viewLifecycleOwner) { logInStatus ->
                validLogin(logInStatus)
            }
            errorMessage.observe(viewLifecycleOwner) {
                showErrorMessage(it)
            }
            forgotPasswordStatus.observe((viewLifecycleOwner)) { forgotPasswordStatus ->
                handleForgotPassword(forgotPasswordStatus)
            }

        }


    }

    private fun handleForgotPassword(forgotPasswordStatus: BaseLoadingStatus?) {
        when (forgotPasswordStatus) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
            }
            BaseLoadingStatus.SUCCEEDED -> {
                Toast.makeText(
                    activity,
                    getString(R.string.email_rs_password_sent, binding.edtEmail.text.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                binding.edtPassword.requestFocus()
                setLoading(false)
            }
            else -> {
                setLoading(false)
            }

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

    private fun showErrorMessage(err: String?) {
        if (err == null)
            return
        if (err.isNotEmpty()) {
            binding.tvErr.text = err
            binding.tvErr.visibility = View.VISIBLE
        } else {
            binding.tvErr.visibility = View.GONE
        }
    }

    private fun validLogin(logInStatus: BaseLoadingStatus) {
        when (logInStatus) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
                binding.btnLogin.isEnabled = false
                // todo loading icon
            }
            BaseLoadingStatus.SUCCEEDED -> {
                setLoading(false)
                launchHome(requireContext())
                activity?.finish()
            }
            BaseLoadingStatus.FAILED -> {
                setLoading(false)
            }
        }
    }

    private fun handlePassword(errorPassword: String?) {
        with(binding) {
            if (!errorPassword.isNullOrEmpty()) {
                inputLayoutPassword.error = errorPassword
                inputLayoutPassword.errorIconDrawable = null
            } else {
                inputLayoutPassword.isErrorEnabled = false
                android.util.Log.d(TAG, "setObservers: password valid")
            }
        }
    }

    private fun handleErrorEmail(errorEmail: String?) {
        with(binding) {
            if (!errorEmail.isNullOrEmpty()) {
                inputLayoutEmail.error = errorEmail
                inputLayoutEmail
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
                    textInputViewModel.checkEmail(edtEmail.text.toString(), 0)
                    textInputViewModel.setLoginFormValid()
                }
            })
            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString(), 0)
                    textInputViewModel.setLoginFormValid()
                }
            })
            inputLayoutEmail.setOnFocusChangeListener { _, _ ->
                viewModel.emptyErrorMessage()
            }
            inputLayoutPassword.setOnFocusChangeListener { _, _ ->
                viewModel.emptyErrorMessage()
            }
        }
    }

    override fun setViews() {
        with(binding) {
            btnLogin.setOnClickListener {
                hideKeyboard()
                viewModel.logInWithEmail(
                    binding.edtEmail.text.toString().trim(),
                    binding.edtPassword.text.toString().trim()
                )
            }

            layoutForgotPassword.setOnClickListener {
                hideKeyboard()
                if (inputLayoutEmail.endIconMode != TextInputLayout.END_ICON_CUSTOM &&
                    binding.edtEmail.text.toString().isNotBlank()
                ) {
                    Log.d(TAG, "setViews: email valid")
                    viewModel.forgotPassword(binding.edtEmail.text.toString().trim())
                } else {
                    Toast.makeText(context, "Please input your email first!", Toast.LENGTH_SHORT).show()
                    edtEmail.requestFocus()
                }
            }

            ivFacebook.setOnClickListener {
                hideKeyboard()
                viewModel.logInWithFacebook(this@LoginFragment)
            }
            ivGoogle.setOnClickListener {
                hideKeyboard()
                viewModel.logInWithGoogle(this@LoginFragment)
            }
        }
    }


    override fun getViewBinding(): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater)
    }

}