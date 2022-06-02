package com.goldenowl.ecommerce.ui.global.profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSettingsBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEEMAIL
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.ui.global.BaseFragment
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.Utils.getDateTime
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.TextInputViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    val TAG = "SettingsFragment"
    private lateinit var userManager: UserManager
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    private val textInputViewModel: TextInputViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun getViewBinding(): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun init() {
        super.init()
        userManager = UserManager.getInstance(requireContext())

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        Log.d(TAG, "init: ${data.data}")
                        val file: Uri? = data.data
                        authViewModel.updateAvatar(file)

                    }
                }
            }
    }


    override fun setViews() {

        binding.layoutUserPassword.visibility =
            if (userManager.logType == TYPEEMAIL) View.VISIBLE else View.GONE
        val settingsManager = SettingsManager(requireContext()).settingManager
        val settingsEditor = settingsManager.edit()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()

        datePicker.addOnPositiveButtonClickListener { time ->
            val date = getDateTime(time)
            Log.d(TAG, "setViews: time=$date")
            binding.edtDob.setText(date)
            userManager.dob = date
        }

        restoreViews(settingsManager)

        with(binding) {

            tvChangePassword.setOnClickListener {
                openBottomSheet()
            }

            ivUserAvatar.setOnClickListener {
                changeAvatar()
            }

            swSale.setOnCheckedChangeListener { _, isChecked ->
                run {
                    settingsEditor.putBoolean(SettingsManager.KEY_NOTIFICATION_SALE, isChecked)
                    settingsEditor.commit()
                }
            }
            swDeliStatus.setOnCheckedChangeListener { _, isChecked ->
                run {
                    settingsEditor.putBoolean(
                        SettingsManager.KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE,
                        isChecked
                    )
                    settingsEditor.commit()
                }
            }
            swNewArrivals.setOnCheckedChangeListener { _, isChecked ->
                run {
                    settingsEditor.putBoolean(
                        SettingsManager.KEY_NOTIFICATION_ARRIVES,
                        isChecked
                    )
                    settingsEditor.commit()
                }
            }

            edtDob.setOnClickListener {
                datePicker.show(parentFragmentManager, "tag");
            }

            edtName.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus && textInputViewModel.errorName.value == null)
                    userManager.name = edtName.text.toString()
            }
        }
    }

    private fun restoreViews(
        settingsManager: SharedPreferences,
    ) {
        val fullName: String? = userManager.name
        val dob: String? = userManager.dob

        with(binding) {
            edtName.setText(fullName)
            edtDob.setText(dob)

            swSale.isChecked =
                settingsManager.getBoolean(SettingsManager.KEY_NOTIFICATION_SALE, true)
            swDeliStatus.isChecked = settingsManager.getBoolean(
                SettingsManager.KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE,
                false
            )
            swNewArrivals.isChecked =
                settingsManager.getBoolean(SettingsManager.KEY_NOTIFICATION_ARRIVES, false)

        }
    }

    private fun changeAvatar() {
        Log.d(TAG, "changeAvatar: start")
        val openGalleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        imageActivityResultLauncher.launch(openGalleryIntent)
    }

    private fun openBottomSheet() {
        val modalBottomSheet = ModalBottomSheet(userManager)

        modalBottomSheet.show(
            requireActivity().supportFragmentManager,
            ModalBottomSheet.TAG
        )
    }

    override fun setObservers() {
        with(textInputViewModel) {

            errorName.observe(viewLifecycleOwner) { errorName ->
                validName(errorName)
            }

            errorDoB.observe(viewLifecycleOwner) { errorDoB ->
                validDoB(errorDoB)
            }

        }

    }

    private fun validDoB(errorDoB: String?) {
        with(binding) {
            if (errorDoB != null) {
                inputLayoutDob.error = errorDoB
            } else {
                inputLayoutDob.isErrorEnabled = false
                inputLayoutDob.endIconMode =
                    TextInputLayout.END_ICON_CUSTOM
                Log.d(TAG, "setObservers: DoB valid")
            }
        }
    }

    private fun validName(errorName: String?) {
        with(binding) {
            if (errorName != null) {
                inputLayoutName.error = errorName
            } else {
                inputLayoutName.isErrorEnabled = false
                inputLayoutName.endIconMode =
                    TextInputLayout.END_ICON_CUSTOM
                Log.d(TAG, "setObservers: name valid")
            }
        }
    }

    override fun setUpListener() {
        super.setUpListener()

        with(binding) {
            edtName.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkName(edtName.text.toString())
                }
            })
            edtDob.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkDoB(edtDob.text.toString())
                }
            })
            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString())
                    textInputViewModel.setLoginFormValid()
                }
            })
        }
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.settings)
    }
}


