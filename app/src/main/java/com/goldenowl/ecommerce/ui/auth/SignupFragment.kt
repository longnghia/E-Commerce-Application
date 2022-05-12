package com.goldenowl.ecommerce.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSignupBinding

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
    }
}