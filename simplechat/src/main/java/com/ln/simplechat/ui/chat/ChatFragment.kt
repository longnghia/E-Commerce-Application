package com.ln.simplechat.ui.chat

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.util.Util
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatFragmentBinding
import com.ln.simplechat.databinding.DialogReactionBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Message
import com.ln.simplechat.observer.chat.ChatAdapterObserver
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
import java.util.concurrent.TimeUnit


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
            val mapMember = viewModel.listMember.value!!.map { it.id to it }.toMap()
            adapter = ChatAdapter(
                requireContext(),
                currentUserId,
                mapMember,
                this
            )
            adapter.onMessageLongClickListener = { view, messageId ->
                createReactionDialog(view, messageId)
            }
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

        binding.input.doAfterTextChanged {
            if (binding.input.text.toString().isEmpty()) {
                binding.btnSend.setImageResource(R.drawable.ic_like)
            } else {
                binding.btnSend.setImageResource(R.drawable.ic_send)
            }
        }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> sendTextMessage()
            }
            true
        }

        binding.btnSend.setOnClickListener {
            if (binding.input.text.toString().isEmpty())
                viewModel.sendReactMessage()
            else
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

    private fun sendTextMessage(msg: String? = null) {
        val msg = msg ?: binding.input.text.toString().trim()
        val timestamp = System.currentTimeMillis()

        val lastItem = viewModel.listMessage.value?.lastOrNull()
        val lastTimestamp = lastItem?.timestamp ?: timestamp

        val timeIdle = timestamp - lastTimestamp
        val idleBreak = timeIdle > TIME_IDLE_BREAK

        if (timestamp - lastTimestamp > TIME_LINE_BREAK) {
            viewModel.sendMessage(
                channelId,
                Message(Util.autoId(), isTimeline = true, timestamp = timestamp)
            )
            viewModel.sendMessage(
                channelId,
                Message(
                    Util.autoId(),
                    currentUserId,
                    msg,
                    idleBreak = idleBreak
                )
            )
        } else
            viewModel.sendMessage(
                channelId,
                Message(
                    Util.autoId(),
                    currentUserId,
                    msg,
                    timestamp = timestamp,
                    idleBreak = idleBreak
                )
            )
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

        val TIME_LINE_BREAK = TimeUnit.HOURS.toMillis(6)  // add new message timeline for 6 hours
        val TIME_IDLE_BREAK = TimeUnit.MINUTES.toMillis(2)  // set chat idle for 2 min

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

    @SuppressLint("ClickableViewAccessibility")
    private fun createReactionDialog(message: View, messageId: String) {
        val dialog = Dialog(requireActivity())
        val reactionBinding = DialogReactionBinding.inflate(layoutInflater, null, false)
        val reaction = reactionBinding.reaction
        dialog.window?.attributes?.gravity = Gravity.TOP
        val location = IntArray(2)
        message.getLocationOnScreen(location)
        val marginTop = location[1] - 250
        val param = reaction.layoutParams as LinearLayout.LayoutParams
        param.setMargins(0, marginTop, 0, 0)
        reaction.layoutParams = param
        reactionBinding.layoutReact.setOnClickListener {
            dialog.dismiss()
        }
        message.alpha = 0.5f

        val delay: Long = 150
        val reactions = listOf(
            reactionBinding.r1,
            reactionBinding.r2,
            reactionBinding.r3,
            reactionBinding.r4,
            reactionBinding.r5,
            reactionBinding.r6,
        )
        reactions.forEachIndexed { index, imageView ->
            imageView.fadeIn(delay * (index + 1))
        }

        var lastFocusedReact: ImageView? = null

        reaction.setOnTouchListener { _, event ->
            var react: ImageView?
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    react = checkTouch(event, reactions)
                    if (react != null) {
                        if (lastFocusedReact != react)
                            lastFocusedReact?.animate()?.setDuration(100)?.rotation(0f)
                        lastFocusedReact = react
                        react.rotation = -30f
                    }
                }
                MotionEvent.ACTION_UP -> {
                    lastFocusedReact?.animate()?.setDuration(100)?.rotation(0f)?.withEndAction {
                        viewModel.pushReact(messageId, reactions.indexOf(lastFocusedReact))
                        dialog.dismiss()
                    }
                }
            }

            false
        }

        dialog.apply {
            setContentView(reactionBinding.root)
            setOnDismissListener {
                message.alpha = 1f
            }
            setCancelable(true)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window?.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))
            show()
        }
    }

    private fun checkTouch(event: MotionEvent, reactions: List<ImageView>): ImageView? {
        for (index in reactions.lastIndex downTo 0) {
            if (event.x >= reactions[index].x) {
                return reactions[index]
            }
        }
        return null
    }
}

interface ChatListener {
    fun openPreview(position: Int, data: List<ChatMedia>)
}