package com.goldenowl.ecommerce.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.databinding.FragmentLoginBinding
import com.goldenowl.ecommerce.launchHome
import com.goldenowl.ecommerce.models.data.SessionManager

class LoginFragment : Fragment() {
    private  val TAG="LoginFragment"
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentLoginBinding.inflate(inflater)

        setViews(requireActivity())
        return binding.root
    }

    private fun setViews(context: Context) {
        binding.btnLogin.setOnClickListener {
            logIn(context)
            launchHome(context)
        }
    }

    private fun logIn(context: Context) {
        Log.d(TAG, "logIn: info = ${binding.edtEmail.text} , ${binding.edtPassword.text}")
        val (email, password) = arrayOf(binding.edtEmail.text.toString(), binding.edtPassword.text.toString())

        val sessionManager = SessionManager(context)
        sessionManager.createLoginSession("1", email, password )

//        if (sessionManager.isLoggedIn()) {
//            launchHome(context)
//            finish()
//        } else {
//            val intent = Intent(this, LoginSignupActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

//        TODO("Not yet implemented")
    }
}