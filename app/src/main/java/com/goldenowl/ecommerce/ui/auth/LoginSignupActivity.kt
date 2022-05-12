package com.goldenowl.ecommerce.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.goldenowl.ecommerce.R

class LoginSignupActivity : AppCompatActivity() {
    private val TAG ="LoginSignupActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)
        Log.d(TAG, "onCreate: ")
    }
}