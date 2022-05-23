package com.goldenowl.ecommerce.ui.home.profile

import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.activityViewModels
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSettingsBinding
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    val TAG = "SettingsFragment"

    override fun setViewBinding(): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(layoutInflater)
    }

    private val viewModel: AuthViewModel by activityViewModels()

    override fun setViews() {

        val settingsManager = SettingsManager(requireContext()).settingManager
        val sessionManager = SessionManager(requireContext()).userSession
        val settingsEditor = settingsManager.edit()
        val sessionEditor = sessionManager.edit()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()

        datePicker.addOnPositiveButtonClickListener { time ->
            val date = getDateTime(time)
            Log.d(TAG, "setViews: time=$date")
            binding.edtDob.setText(date)
            sessionEditor.putString(SessionManager.KEY_DOB, date).commit()
        }

        restoreViews(settingsManager, sessionManager)

        with(binding) {
            topAppBar.toolbar.title = getString(R.string.settings)

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
                    settingsEditor.putBoolean(SettingsManager.KEY_NOTIFICATION_ARRIVES, isChecked)
                    settingsEditor.commit()
                }
            }

            edtDob.setOnClickListener {
                datePicker.show(parentFragmentManager, "tag");
            }

            edtName.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus && viewModel.errorName.value == null)
                    sessionEditor.putString(SessionManager.KEY_FULL_NAME, edtName.text.toString())
                        .commit()
            }
        }
    }

    private fun getDateTime(time: Long?): String? {
        if (time == null) {
            return null
        }
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(time)
        return sdf.format(netDate)
    }

    private fun restoreViews(
        settingsManager: SharedPreferences,
        sessionManager: SharedPreferences
    ) {
        val fullName: String? = sessionManager.getString(SessionManager.KEY_FULL_NAME, "")
        val dob: String? = sessionManager.getString(SessionManager.KEY_DOB, "")

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
    }

    private fun openBottomSheet() {
        val modalBottomSheet = ModalBottomSheet()

        modalBottomSheet.show(
            requireActivity().supportFragmentManager,
            ModalBottomSheet.TAG
        )
    }

    override fun observeViews() {
        with(binding) {
            with(viewModel) {

                errorName.observe(viewLifecycleOwner) { errorName ->
                    if (errorName != null) {
                        inputLayoutName.error = errorName
                    } else {
                        inputLayoutName.isErrorEnabled = false
                        inputLayoutName.endIconMode =
                            TextInputLayout.END_ICON_CUSTOM
                        Log.d(TAG, "observeViews: name valid")
                    }
                }

//                errorPassword.observe(viewLifecycleOwner) { errorPassword ->
//                    if (errorPassword != null) {
//                        inputLayoutPassword.error = errorPassword
//                        inputLayoutPassword.errorIconDrawable = null
//                    } else {
//                        inputLayoutPassword.isErrorEnabled = false
//
//                        Log.d(TAG, "observeViews: password valid")
//                    }
//                }

                errorDoB.observe(viewLifecycleOwner) { errorDoB ->
                    if (errorDoB != null) {
                        inputLayoutDob.error = errorDoB
                    } else {
                        inputLayoutDob.isErrorEnabled = false
                        inputLayoutDob.endIconMode =
                            TextInputLayout.END_ICON_CUSTOM
                        Log.d(TAG, "observeViews: DoB valid")
                    }
                }

            }

        }
    }

    override fun setUpListener() {
        super.setUpListener()

        with(binding) {
            edtName.addTextChangedListener(object : TextWatcher {
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
                    viewModel.checkName(edtName.text.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            edtDob.addTextChangedListener(object : TextWatcher {
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
                    viewModel.checkDoB(edtDob.text.toString())
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

}


