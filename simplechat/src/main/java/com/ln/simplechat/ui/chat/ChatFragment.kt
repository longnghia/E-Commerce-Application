package com.ln.simplechat.ui.chat

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatFragmentBinding
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.Message
import com.ln.simplechat.observer.chat.ChatAdapterObserver
import com.ln.simplechat.observer.chat.SendButtonObserver
import com.ln.simplechat.ui.viewBindings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.chat_fragment) {
    private val binding by viewBindings(ChatFragmentBinding::bind)
    val currentUserId = "DPql1uxYezTe4m6HrP0UMlm3Ikh2"

    //    private lateinit var adapter: ChatAdapter
    private lateinit var mItemsCollection: CollectionReference
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var channelId: String
    private lateinit var channel: Channel
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private lateinit var manager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channel = (arguments?.get(CHANNEL) ?: return) as Channel
        channelId = channel.id

        viewModel.getListMember(channel.listUser)
        mFirestore = FirebaseFirestore.getInstance();
        mItemsCollection = mFirestore.collection("items");
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.input.addTextChangedListener(SendButtonObserver(binding.btnSend))
        binding.input.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> sendTextMessage()
            }
            false
        }

        binding.btnSend.setOnClickListener {
            sendTextMessage()
        }

        viewModel.listMessage.observe(viewLifecycleOwner) {
            if (viewModel.listMember.value == null)
                return@observe
            adapter = ChatAdapter(currentUserId, viewModel.listMember.value!!)
            adapter.submitList(it)
            manager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            binding.rcvMessages.apply {
                layoutManager = manager
                binding.rcvMessages.adapter = this@ChatFragment.adapter
            }
            adapter.registerAdapterDataObserver(ChatAdapterObserver(binding.rcvMessages, adapter, manager))
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTextMessage() {
        viewModel.sendTextMessage(channelId, Message("1", currentUserId, binding.input.text.toString().trim()))
        binding.input.setText("")
    }

    override fun onPause() {
        viewModel.stopListening()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startListening()
    }

    companion object {
        const val CHANNEL = "CHANNEL"
        fun newInstance(channel: Channel) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(CHANNEL, channel)
                }
            }
    }
}