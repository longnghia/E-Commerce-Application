package com.goldenowl.ecommerce.ui.auth

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentForgotPasswordBinding
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.TextValidation
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*


class ForgotPasswordFragment : BaseAuthFragment<FragmentForgotPasswordBinding>() {

    var email = ""

    override fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.forgot_password_2)
        binding.topAppBar.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun setObservers() {

    }


    override fun setupListeners() {
        val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        val debounceJob: Job? = null
        var lastInput = ""

        binding.edtEmail.addTextChangedListener(object : FieldValidators.TextChange {
            override fun onTextChanged(s: CharSequence?) {
                val newText = s.toString()
                debounceJob?.cancel()
                if (lastInput != newText) {
                    lastInput = newText
                    uiScope.launch {
                        delay(500)
                        val err = TextValidation.validateEmail(s.toString()).isNotEmpty()
                        binding.btnSend.isEnabled = !err
                        if (err) {
                            binding.inputLayoutEmail.isErrorEnabled = true
                            binding.tvEmailErr.visibility = View.VISIBLE
                        } else {
                            binding.inputLayoutEmail.isErrorEnabled = false
                            binding.inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
                            binding.tvEmailErr.visibility = View.GONE
                            email = newText
                        }
                    }
                }
            }
        })

        viewModel.forgotPasswordStatus.observe(viewLifecycleOwner) {
            when (it) {
                BaseLoadingStatus.LOADING -> binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                BaseLoadingStatus.SUCCEEDED -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.forgot_password_2)
                        .setMessage(getString(R.string.email_rs_password_sent, email))
                        .setPositiveButton(R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            findNavController().navigateUp()
                        }
                        .show()
                }
                else -> binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
            }
        }
    }

    override fun setViews() {
        with(binding) {
            btnSend.setOnClickListener {
                hideKeyboard()
                viewModel.forgotPassword(binding.edtEmail.text.toString().trim())
            }
        }
    }


    override fun getViewBinding(): FragmentForgotPasswordBinding {
        return FragmentForgotPasswordBinding.inflate(layoutInflater)
    }
}