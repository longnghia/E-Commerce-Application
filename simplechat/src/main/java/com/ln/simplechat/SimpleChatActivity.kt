package com.ln.simplechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.ln.simplechat.databinding.ActivitySimpleChatBinding
import com.ln.simplechat.services.OnTaskRemoveService
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.main.MainFragment
import com.ln.simplechat.ui.viewBindings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimpleChatActivity : AppCompatActivity(R.layout.activity_simple_chat), NavigationController {
    private val binding by viewBindings(ActivitySimpleChatBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
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
        onNewIntent(intent);
    }

    fun setSystemBarColor(colorRes: Int) {
        window.apply {
            statusBarColor = getColor(colorRes)
            navigationBarColor = getColor(colorRes)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val id = intent.data?.lastPathSegment
                if (id != null) {
                    openChat(id)
                }
            }
            Intent.ACTION_SEND -> {
                // implement later
            }
        }
    }

    override fun openChat(channelId: String) {
        supportFragmentManager.popBackStack(ChatFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            addToBackStack(ChatFragment.TAG)
            replace(R.id.container, ChatFragment.newInstance(channelId))
        }
    }
}