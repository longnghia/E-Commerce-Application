package com.goldenowl.ecommerce.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentProfileBinding
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity

class ProfileFragment : Fragment() {
    private val TAG="Profile Fagment"
    private  lateinit var binding: FragmentProfileBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater)

        setViews()

        return binding.root
    }

    private fun setViews() {
        binding.authBtn.setOnClickListener {
            Log.d(TAG, "setViews: goto Login Signup Activity")
            val loginIntent = Intent(activity, LoginSignupActivity::class.java)
            logOut()
            context?.startActivity(loginIntent)
            activity?.finish()
        }
    }

    private fun logOut() {
        val sessionManager = SessionManager(requireActivity())
        sessionManager.logoutFromSession()

    }
}