package com.goldenowl.ecommerce.ui.home.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.goldenowl.ecommerce.databinding.BottomSheetChangePasswordBinding
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout

class ModalBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetChangePasswordBinding
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreateView: creating view")
        binding = BottomSheetChangePasswordBinding.inflate(inflater)
        setViews()
        setupListeners()
        observeViews()
        return binding.root
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setViews()
//        setupListeners()
//
//        Log.d(TAG, "onCreate: ModalBottomSheet")
//    }

    private fun observeViews() {
        with(binding) {
            with(viewModel) {

                errorOldPassword.observe(viewLifecycleOwner) { errorOldPassword ->
                    if (errorOldPassword != null) {
                        inputLayoutOldPassword.error = errorOldPassword
                        inputLayoutOldPassword.errorIconDrawable = null
                    } else {
                        inputLayoutOldPassword.isErrorEnabled = false
                        Log.d(TAG, "observeViews: old password valid")
                    }
                }

                errorPassword.observe(viewLifecycleOwner) { errorNewPassword ->
                    if (errorNewPassword != null) {
                        inputLayoutNewPassword.error = errorNewPassword
                        inputLayoutNewPassword.errorIconDrawable = null
                    } else {
                        inputLayoutNewPassword.isErrorEnabled = false
                        Log.d(TAG, "observeViews: new password valid")
                    }
                }

                errorRePassword.observe(viewLifecycleOwner) { errorRePassword ->
                    if (errorRePassword != null) {
                        inputLayoutRepeatPassword.error = errorRePassword
                        inputLayoutRepeatPassword.errorIconDrawable = null
                    } else {
                        inputLayoutRepeatPassword.isErrorEnabled = false
                        Log.d(TAG, "observeViews: repeat password valid")
                    }
                }

                changePasswordValid.observe(viewLifecycleOwner) { changePasswordValid ->
                    btnSavePassword.isEnabled = changePasswordValid
                }

            }

        }
    }

    private fun setupListeners() {
        with(binding) {
            edtOldPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.checkOldPassword(edtOldPassword.text.toString(), "123456")
                    viewModel.setChangePasswordValid()
                }
            }
            edtNewPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.checkPassword(edtNewPassword.text.toString())
                    viewModel.setChangePasswordValid()
                }
            }
//            edtOldPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(
//                    s: CharSequence, start: Int,
//                    before: Int, count: Int
//                ) {
//                    viewModel.checkOldPassword(edtOldPassword.text.toString(), "123456")
//                    viewModel.setChangePasswordValid()
//                }
//
//                override fun afterTextChanged(s: Editable?) {
//                }
//            })
//            edtNewPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(
//                    s: CharSequence, start: Int,
//                    before: Int, count: Int
//                ) {
//                    viewModel.checkPassword(edtNewPassword.text.toString())
//                    viewModel.setChangePasswordValid()
//
//                }
//
//                override fun afterTextChanged(s: Editable?) {
//                }
//            })
            edtRepeatPassword.addTextChangedListener(object : TextWatcher {
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
                    viewModel.checkRePassword(
                        edtNewPassword.text.toString(),
                        edtRepeatPassword.text.toString()
                    )
                    viewModel.setChangePasswordValid()
                }

                override fun afterTextChanged(s: Editable?) {
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
                savePassword()
            }


//            inputLayoutOldPassword.setEndIconOnClickListener { togglePassword() }
        }
    }

    private fun savePassword() {
        Log.d(TAG, "savePassword: save password")
    }

    private fun togglePassword() {
        with(binding) {
            inputLayoutOldPassword.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            inputLayoutNewPassword.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

        }
    }

    private fun forgotPassword() {
        Log.d(TAG, "forgotPassword: forgot password")
    }

    private fun changePassword() {
        val newPassword = binding.edtNewPassword.text.toString()
        Log.d(TAG, "changePassword: new password = $newPassword")
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val mView = super.onCreateDialog(savedInstanceState)
//        mView.setContentView(layoutInflater.inflate(R.layout.bottom_sheet_change_password, null))
//        return mView
//    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}