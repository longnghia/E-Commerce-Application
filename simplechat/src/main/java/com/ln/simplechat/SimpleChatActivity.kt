package com.ln.simplechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import com.ln.simplechat.databinding.ActivitySimpleChatBinding
import com.ln.simplechat.services.OnTaskRemoveService
import com.ln.simplechat.ui.main.MainFragment
import com.ln.simplechat.ui.viewBindings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimpleChatActivity : AppCompatActivity(R.layout.activity_simple_chat) {
    private val binding by viewBindings(ActivitySimpleChatBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.container, MainFragment())
            }
        }
        try {
            startService(Intent(this, OnTaskRemoveService::class.java))
        } catch (e: Exception) {
            Log.e(SimpleChatActivity::class.java.simpleName, "onCreate: Fail to start Service", e)
        }
    }

    fun setSystemBarColor(colorRes: Int) {
        window.apply {
            statusBarColor = getColor(colorRes)
            navigationBarColor = getColor(colorRes)
        }
    }
}