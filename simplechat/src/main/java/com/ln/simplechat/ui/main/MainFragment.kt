package com.ln.simplechat.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.R
import com.ln.simplechat.databinding.MainFragmentBinding
import com.ln.simplechat.model.Channel
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.viewBindings

class MainFragment : Fragment(R.layout.main_fragment) {
    private val binding by viewBindings(MainFragmentBinding::bind)
    private lateinit var adapter: ChannelAdapter

    val db: FirebaseFirestore = Firebase.firestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChannelAdapter { channel ->
            activity?.supportFragmentManager?.commit {
                addToBackStack("FRAGMENT_CHAT")
                replace(R.id.container, ChatFragment.newInstance(channel))
            }
        }
        binding.contacts.apply {
            setHasFixedSize(true)
            adapter = this@MainFragment.adapter
        }
        adapter.submitList(
            listOf(
                Channel(
                    "4be9efc7-69e1-47aa-990c-1ddaafaf0500",
                    "channel1",
                    listOf("DPql1uxYezTe4m6HrP0UMlm3Ikh2", "PVKt9L4g67VR8ExdWji9MYM8eII2"),
                    icon = "https://cdn-icons-png.flaticon.com/512/149/149071.png"
                ),
                Channel(
                    "4be9efc7-69e1-47aa-990c-1ddaafaf0500",
                    "channel2",
                    listOf("DPql1uxYezTe4m6HrP0UMlm3Ikh2", "PVKt9L4g67VR8ExdWji9MYM8eII2"),
                    icon = "https://cdn-icons-png.flaticon.com/512/149/149071.png"
                ),
            )
        )
    }
}