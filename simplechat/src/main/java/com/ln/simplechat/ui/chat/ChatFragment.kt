package com.ln.simplechat.ui.chat

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.util.Util
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatFragmentBinding
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Message
import com.ln.simplechat.observer.chat.ChatAdapterObserver
import com.ln.simplechat.observer.chat.SendButtonObserver
import com.ln.simplechat.ui.preview.PicturePreviewFragment
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.GlideEngine
import com.ln.simplechat.utils.media.ImageFileCompressEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.chat_fragment), ChatListener {
    private val binding by viewBindings(ChatFragmentBinding::bind)

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "DPql1uxYezTe4m6HrP0UMlm3Ikh2" // recheck

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
        viewModel.initData(channel)
        mFirestore = FirebaseFirestore.getInstance();
        mItemsCollection = mFirestore.collection("items");
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObservers()
    }

    private fun setObservers() {
        viewModel.listMessage.observe(viewLifecycleOwner) {
            if (viewModel.listMember.value == null)
                return@observe
            adapter = ChatAdapter(requireContext(), currentUserId, viewModel.listMember.value!!, this)
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

    private fun setViews() {
        binding.input.addTextChangedListener(SendButtonObserver(binding.btnSend))
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> sendTextMessage()
            }
            false
        }

        binding.btnSend.setOnClickListener {
            sendTextMessage()
        }

        binding.btnGallery.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(SelectMimeType.ofAll())
                .setImageEngine(GlideEngine.createGlideEngine())
                .isWithSelectVideoImage(true)
                .setMaxSelectNum(MAX_MEDIA)
                .setMaxVideoSelectNum(MAX_VIDEO)
                .isGif(true)
                .setCompressEngine(ImageFileCompressEngine())
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>) {
                        viewModel.sendImagesMessage(requireContext(), result)
                    }

                    override fun onCancel() {}
                })
        }
    }

    private fun sendTextMessage() {
        viewModel.sendMessage(channelId, Message(Util.autoId(), currentUserId, binding.input.text.toString().trim()))
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
        const val TAG = "CHAT_FRAGMENT"
        const val CHANNEL = "CHANNEL"

        const val MAX_MEDIA = 10
        const val MAX_VIDEO = 10

        fun newInstance(channel: Channel) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(CHANNEL, channel)
                }
            }
    }

    override fun openPreview(position: Int, data: List<ChatMedia>) {
        activity?.supportFragmentManager?.commit {
            addToBackStack(PicturePreviewFragment.TAG)
            replace(R.id.container, PicturePreviewFragment.newInstance(position, java.util.ArrayList(data)))
        }
    }
}

interface ChatListener {
    fun openPreview(position: Int, data: List<ChatMedia>)
}