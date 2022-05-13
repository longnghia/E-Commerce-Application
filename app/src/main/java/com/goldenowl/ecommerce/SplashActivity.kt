package com.goldenowl.ecommerce

import android.content.Intent
import android.net.sip.SipErrorCode.TIME_OUT
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setLaunchScreenTimeOut()
    }

    private fun setLaunchScreenTimeOut() {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                startActivity()
            }, TIME_OUT)
        }
    }

    private fun startActivity() {

        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val userData: HashMap<String, String?> = sessionManager.getUserDataFromSession()
            Log.d(TAG, "startPreferredActivity: logged in as ${userData.get("userName")}")
            launchHome(this)
            finish()
        } else {
            val intent = Intent(this, LoginSignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkFirstLaunch() {

    }

    companion object {
        private const val TIME_OUT: Long = 500
    }
}