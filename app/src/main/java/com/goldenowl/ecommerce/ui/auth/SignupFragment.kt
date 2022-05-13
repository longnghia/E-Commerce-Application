package com.goldenowl.ecommerce.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.TutorialActivity
import com.goldenowl.ecommerce.databinding.FragmentSignupBinding
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.models.data.SettingsManager

class SignupFragment : Fragment() {

    private val TAG = "SignupFragment"
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentSignupBinding.inflate(layoutInflater)

        setViews()

        return binding.root
    }


    private fun setViews() {
        binding.btnLogin.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.login_dest)
        )
        binding.btnSignup.setOnClickListener(View.OnClickListener {
            val sessionManager = SessionManager(requireActivity())
            sessionManager.createLoginSession("123", binding.edtEmail.text.toString(), binding.edtPassword.text.toString())

            val settingsManager = SettingsManager(requireActivity())
            if (settingsManager.getFirstLaunch()) {
                Log.d(TAG, "startActivity: firstlaunch = "+ settingsManager.getFirstLaunch())
                val intentTutorial = Intent(activity, TutorialActivity::class.java)
                startActivity(intentTutorial)
            }

        })
    }
}