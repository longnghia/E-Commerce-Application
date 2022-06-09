package com.goldenowl.ecommerce.ui.global.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ModalBottomSheetChangePasswordBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.TextInputViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetChangePassword(private val userManager : UserManager) : BottomSheetDialogFragment() {
    private lateinit var binding: ModalBottomSheetChangePasswordBinding
    private val textInputViewModel: TextInputViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = ModalBottomSheetChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
        setupListeners()
        setObservers()
    }

    private fun setObservers() {
        with(textInputViewModel) {
            errorOldPassword.observe(viewLifecycleOwner) { errorOldPassword ->
                validOldPassword(errorOldPassword)
            }

            errorLoginPassword.observe(viewLifecycleOwner) { errorNewPassword ->
                validNewPassword(errorNewPassword)
            }

            errorRePassword.observe(viewLifecycleOwner) { errorRePassword ->
                validRepeatPassword(errorRePassword)
            }

            changePasswordValid.observe(viewLifecycleOwner) { changePasswordValid ->
                binding.btnSavePassword.isEnabled = changePasswordValid
            }

        }
        authViewModel.changePasswordStatus.observe(viewLifecycleOwner) { changePasswordStatus ->
            onChangePassword(changePasswordStatus)
        }
        authViewModel.forgotPasswordStatus.observe(viewLifecycleOwner){
            onForgotPassword(it)
        }
    }

    private fun onForgotPassword(forgotPwStatus: BaseLoadingStatus?) {
        when (forgotPwStatus) {
            BaseLoadingStatus.LOADING -> {
                binding.layoutLoading.loadingFrameLayout.visibility  =View.VISIBLE
            }

            BaseLoadingStatus.SUCCEEDED -> {
                Toast.makeText(activity, getString(R.string.email_rs_password_sent, userManager.email), Toast.LENGTH_SHORT).show()
                this@BottomSheetChangePassword.dismiss()
            }
            else -> binding.layoutLoading.loadingFrameLayout.visibility  =View.INVISIBLE
        }
    }

    private fun onChangePassword(changePasswordStatus: BaseLoadingStatus) {
        when (changePasswordStatus) {
            BaseLoadingStatus.LOADING -> {
                binding.layoutLoading.loadingFrameLayout.visibility  =View.VISIBLE
            }
            BaseLoadingStatus.SUCCEEDED -> {
                Toast.makeText(activity, getText(R.string.change_password_success), Toast.LENGTH_SHORT).show()
                this@BottomSheetChangePassword.dismiss()
            }
        }
    }

    private fun validRepeatPassword(errorRePassword: String?) {
        with(binding) {
            if (errorRePassword != null) {
                inputLayoutRepeatPassword.error = errorRePassword
                inputLayoutRepeatPassword.errorIconDrawable = null
            } else {
                inputLayoutRepeatPassword.isErrorEnabled = false
                Log.d(TAG, "setObservers: repeat password valid")
            }
        }
    }

    private fun validNewPassword(errorNewPassword: String?) {
        with(binding) {
            if (errorNewPassword != null) {
                inputLayoutNewPassword.error = errorNewPassword
                inputLayoutNewPassword.errorIconDrawable = null
            } else {
                inputLayoutNewPassword.isErrorEnabled = false
                Log.d(TAG, "setObservers: new password valid")
            }
        }
    }

    private fun validOldPassword(errorOldPassword: String?) {
        with(binding) {

            if (errorOldPassword != null) {
                inputLayoutOldPassword.error = errorOldPassword
                inputLayoutOldPassword.errorIconDrawable = null
            } else {
                inputLayoutOldPassword.isErrorEnabled = false
                Log.d(TAG, "setObservers: old password valid")
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            edtOldPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    textInputViewModel.checkOldPassword(edtOldPassword.text.toString(), userManager.hash)
                    textInputViewModel.setChangePasswordValid()
                }
            }
            edtNewPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    textInputViewModel.checkPassword(edtNewPassword.text.toString(), 0)
                    textInputViewModel.setChangePasswordValid()
                }
            }
            edtRepeatPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkRePassword(
                        edtNewPassword.text.toString(),
                        edtRepeatPassword.text.toString()
                    )
                    textInputViewModel.setChangePasswordValid()
                }
            })
        }
    }

    private fun setViews() {
        with(binding) {
            btnSavePassword.setOnClickListener {
                changePassword()
            }

            tvForgotPassword.setOnClickListener {
                forgotPassword()
            }

            btnSavePassword.setOnClickListener {
                authViewModel.changePassword(edtOldPassword.text.toString(), edtNewPassword.text.toString())
            }
        }
    }


    private fun forgotPassword() {
        Log.d(TAG, "forgotPassword: forgot password")
        authViewModel.forgotPassword(userManager.email)
    }

    private fun changePassword() {
        val newPassword = binding.edtNewPassword.text.toString()
        Log.d(TAG, "changePassword: new password = $newPassword")
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }
}