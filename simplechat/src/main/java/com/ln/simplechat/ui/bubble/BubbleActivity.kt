package com.ln.simplechat.ui.bubble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ActivitySimpleChatBinding
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.viewBindings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BubbleActivity : AppCompatActivity(R.layout.activity_simple_chat) {
    private val binding by viewBindings(ActivitySimpleChatBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelID = intent.data?.lastPathSegment ?: return
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                setCustomAnimations(R.anim.ps_anim_fade_in, R.anim.ps_anim_fade_out)
                replace(R.id.container, ChatFragment.newInstance(channelID))
            }
        }
    }
}