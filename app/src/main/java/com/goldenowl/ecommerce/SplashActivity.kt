package com.goldenowl.ecommerce

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goldenowl.ecommerce.models.data.SessionManager
import com.goldenowl.ecommerce.models.data.SettingsManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
        checkSesssion()
//        printHashKey()
    }

    private fun checkSesssion() {
        val sessionManager = SessionManager(this)
        Log.d(TAG, "checkSesssion: login: ${sessionManager.isLoggedIn()} name=${sessionManager.getUserDataFromSession().get("userName")}")
    }

    private fun setLaunchScreenTimeOut() {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                startActivity()
            }, TIME_OUT)
        }
    }

    private fun startActivity() {

        val settingsManager = SettingsManager(this)
        if (settingsManager.getFirstLaunch()) {
            Log.d(TAG, "startActivity: firstlaunch = " + settingsManager.getFirstLaunch())
            val intentTutorial = Intent(this, TutorialActivity::class.java)
            startActivity(intentTutorial)
        } else {
            launchHome(this)
        }
        finish()
//        val sessionManager = SessionManager(this)
//        if (sessionManager.isLoggedIn()) {
//            val userData: HashMap<String, String?> = sessionManager.getUserDataFromSession()
//            Log.d(TAG, "startPreferredActivity: logged in as ${userData.get("userName")}")
//            launchHome(this)
//            finish()
//        } else {
////            val intent = Intent(this, LoginSignupActivity::class.java)
////            startActivity(intent)
//            launchHome(this)
//            finish()
//        }
    }


    companion object {
        private const val TIME_OUT: Long = 100
    }

    fun printHashKey(){
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }
}