package com.ln.simplechat.ui.chat

import android.app.NotificationManager
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.util.Util
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatFragmentBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Message
import com.ln.simplechat.observer.chat.ChatAdapterObserver
import com.ln.simplechat.observer.chat.SendButtonObserver
import com.ln.simplechat.ui.preview.PicturePreviewFragment
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.*
import com.ln.simplechat.utils.media.ImageFileCompressEngine
import com.ln.simplechat.utils.media.MyOnRecordAudioInterceptListener
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.avatarview.coil.loadImage


@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.chat_fragment), ChatListener {
    private val binding by viewBindings(ChatFragmentBinding::bind)

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "DPql1uxYezTe4m6HrP0UMlm3Ikh2" // recheck

    private lateinit var channelId: String
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private lateinit var manager: LinearLayoutManager
    var bubble = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.slide_bottom)

        bubble = arguments?.getBoolean(IS_BUBBLE) ?: false
        channelId = arguments?.getString(CHANNEL_ID) ?: ""
        if (channelId.isEmpty()) {
            parentFragmentManager.popBackStack()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initData(channelId)

        setViews()
        setObservers()
    }

    private fun setObservers() {
        viewModel.channel.observe(viewLifecycleOwner) {
            when (it) {
                is MyResult.Error -> {
                    Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is MyResult.Success -> {
                    viewModel.initNotificationHelper(it.data)
                }
            }
        }
        viewModel.listMember.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.avatarView.loadImage(
                    data = it.filter { member -> member.id != currentUserId }.map { member -> member.avatar }
                )
                viewModel.startListening()
            }
        }
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
        binding.appbar.isGone = bubble
        binding.btnOpenInNew.isVisible = requireContext().canDeviceDisplayBubbles()
        if (!bubble) {
            binding.btnOpenInNew.setOnClickListener {
                if (requireActivity().getBubblePreference() == NotificationManager.BUBBLE_PREFERENCE_NONE)
                    requestBubblePermissions()
                else {
                    viewModel.showAsBubble()
                    if (isAdded) {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
            binding.btnMoreFeature.setOnClickListener {
                /* implement later: change group name, change group avatar, search message */
            }
        }

        binding.input.addTextChangedListener(SendButtonObserver(binding.btnSend))
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> sendTextMessage()
            }
            true
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
                .setSelectMaxFileSize(MAX_FILE_SIZE)
                .setSelectMaxDurationSecond(MAX_VIDEO_LENGTH)
                .isGif(true)
                .setCompressEngine(ImageFileCompressEngine())
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>) {
                        viewModel.sendImagesMessage(requireContext(), result)
                    }

                    override fun onCancel() {}
                })
        }

        binding.btnMore.setOnClickListener {
            showMenu(it, R.menu.popup_menu_chat_more)
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
        viewModel.resumeListening()
    }

    companion object {
        const val TAG = "CHAT_FRAGMENT"
        const val CHANNEL_ID = "channel_id"
        const val IS_BUBBLE = "is_bubble"

        const val MAX_MEDIA = 10
        const val MAX_VIDEO = 10
        const val MAX_AUDIO = 5
        const val MAX_FILE_SIZE = 20 * 1024L
        const val MAX_VIDEO_LENGTH = 60

        fun newInstance(channelID: String, isBubble: Boolean = false) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(CHANNEL_ID, channelID)
                    putBoolean(IS_BUBBLE, isBubble)
                }
            }
    }

    override fun openPreview(position: Int, data: List<ChatMedia>) {
        activity?.supportFragmentManager?.commit {
            addToBackStack(PicturePreviewFragment.TAG)
            replace(
                R.id.container,
                PicturePreviewFragment.newInstance(position, java.util.ArrayList(data))
            )
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = buildMenu(v, menuRes)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.option_select_audio -> {
                    selectAudio()
                }
            }
            true
        }
        popup.show()
    }

    private fun selectAudio() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofAudio())
            .setMaxSelectNum(MAX_AUDIO)
            .setSelectMaxFileSize(MAX_FILE_SIZE)
            .setImageEngine(GlideEngine.createGlideEngine())
            .setRecordAudioInterceptListener(MyOnRecordAudioInterceptListener())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    viewModel.sendAudio(result)
                }

                override fun onCancel() {}
            })
    }
}

interface ChatListener {
    fun openPreview(position: Int, data: List<ChatMedia>)
}