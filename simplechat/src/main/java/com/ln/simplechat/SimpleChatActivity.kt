package com.ln.simplechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.google.firebase.auth.FirebaseAuth
import com.ln.simplechat.application.toast
import com.ln.simplechat.databinding.ActivitySimpleChatBinding
import com.ln.simplechat.services.OnTaskRemoveService
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.main.IntentViewModel
import com.ln.simplechat.ui.main.MainFragment
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimpleChatActivity : AppCompatActivity(R.layout.activity_simple_chat), NavigationController {
    private val binding by viewBindings(ActivitySimpleChatBinding::bind)

    private val intentViewModel: IntentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        try {
            startService(Intent(this, OnTaskRemoveService::class.java))
        } catch (e: Exception) {
            Log.e(SimpleChatActivity::class.java.simpleName, "onCreate: Fail to start Service", e)
        }
        intent?.also {
            when (it.action) {
                ACTION_CREATE_AND_OPEN_CHANNEL -> {
                    val listUser = it.getStringArrayListExtra(EXTRA_LIST_USER)!!
                    intentViewModel.createChannel(listUser)
                    setObserver()
                }
                ACTION_OPEN_BOARD -> {
                    openBoard()
                }
            }
        }
        onNewIntent(intent)
    }

    private fun setObserver() {
        intentViewModel.channelCreated.observe(this) {
            handleChannelCreated(it)
        }
    }

    private fun handleChannelCreated(channelCreated: MyResult<String>?) {
        binding.layoutLoading.loadingFrameLayout.isVisible = channelCreated is MyResult.Loading
        when (channelCreated) {
            is MyResult.Success -> {
                openChat(channelCreated.data, addBackTrack = false)
            }
            is MyResult.Error -> {
                toast(channelCreated.exception.message)
            }
            else -> {}
        }
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

    override val currentUserId: String?
        get() = intent?.getStringExtra(EXTRA_CURRENT_USER) ?: FirebaseAuth.getInstance().currentUser?.uid


    override fun openChat(channelId: String, bubble: Boolean, addBackTrack: Boolean) {
        supportFragmentManager.popBackStack(ChatFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
//            setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_right)
            if (addBackTrack) addToBackStack(ChatFragment.TAG)
            replace(R.id.container, ChatFragment.newInstance(channelId, bubble))
        }
    }

    override fun openBoard() {
        supportFragmentManager.commitNow {
            replace(R.id.container, MainFragment())
        }
    }

    companion object {
        fun createAndOpenChannel(activity: Activity, currentUserId: String, listUser: ArrayList<String>) {
            if (listUser.size < 2) {
                toast(R.string.create_channel_insufficient_member)
                return
            }
            activity.startActivity(
                Intent(activity, SimpleChatActivity::class.java).apply {
                    putExtra(EXTRA_CURRENT_USER, currentUserId)
                    putStringArrayListExtra(EXTRA_LIST_USER, listUser)
                    action = ACTION_CREATE_AND_OPEN_CHANNEL
                }
            )
        }

        const val ACTION_CREATE_AND_OPEN_CHANNEL = "create-and-open-channel"
        const val ACTION_OPEN_BOARD = "open-open-board"
        const val EXTRA_LIST_USER = "list-user"
        const val EXTRA_CURRENT_USER = "current-user"
    }
}