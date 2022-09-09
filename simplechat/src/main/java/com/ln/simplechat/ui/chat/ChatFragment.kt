package com.ln.simplechat.ui.chat

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.util.Util
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatFragmentBinding
import com.ln.simplechat.databinding.DialogReactionBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.observer.chat.ChatAdapterObserver
import com.ln.simplechat.ui.chat.swipe.MessageSwipeActions
import com.ln.simplechat.ui.chat.swipe.MessageSwipeCallback
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
import kotlinx.android.synthetic.main.item_message_quote.view.*
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

    private lateinit var messageRcv: RecyclerView
    private lateinit var bottomScroll: ImageView
    private lateinit var tvNewMessage: TextView
    private lateinit var quoteLayout: LinearLayout

    private var isAtBottom = true
    private var firstLoad = true
    private var showNewMessage = false

    private var showingQuote: Boolean = false
    private var mQuoted: Message? = null

    private var messages: List<Message> = emptyList()
    private var mapMember: Map<String, Member> = emptyMap()

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
        setupNotification()
    }

    private fun setupNotification() {

        val channelId = getString(R.string.channel_chat)
        val channelName = getString(R.string.channel_chat_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(requireContext(), NotificationManager::class.java)
            val chatChannel = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            chatChannel.description = getString(R.string.channel_chat_description)
            chatChannel.enableLights(false)
            chatChannel.enableVibration(false)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val audioUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + requireContext().packageName + "/" + R.raw.messenger_chat_sound
            )
            chatChannel.setSound(audioUri, audioAttributes)
            notificationManager?.createNotificationChannel(chatChannel)
        }

/*  todo: notification intent

        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
*/

        Firebase.messaging.subscribeToTopic(channelId)
            .addOnCompleteListener { task ->
                Log.d(TAG, "subscribed To Topic $channelId")
            }
        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "Get token successfully= $token")
        })
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

                mapMember = it.associateBy { member -> member.id }
                adapter = ChatAdapter(
                    requireContext(),
                    currentUserId,
                    mapMember,
                    this
                )
                adapter.onMessageLongClickListener = { view, messageId ->
                    createReactionDialog(view, messageId)
                }
                adapter.onQuoteClickListener = { position ->
                    messageRcv.scrollToPosition(position)
                    messageRcv.postDelayed(
                        {
                            val viewHolder = messageRcv.findViewHolderForAdapterPosition(position)
                            viewHolder?.itemView?.startAnimation(
                                AnimationUtils.loadAnimation(
                                    context,
                                    R.anim.anim_wobble
                                )
                            )
                        }, 100
                    )
                }
                manager = LinearLayoutManager(requireContext()).apply {
                    stackFromEnd = true
                }
                val observer = ChatAdapterObserver(
                    messageRcv,
                    adapter,
                    manager
                ) { unSeenMessages ->
                    if (unSeenMessages > 0) {
                        showNewMessage = true
                        showNewMessage()
                        tvNewMessage.text =
                            resources.getQuantityString(R.plurals.num_new_message, unSeenMessages, unSeenMessages)
                    }
                }

                adapter.registerAdapterDataObserver(observer)
                messageRcv.apply {
                    layoutManager = manager
                    adapter = this@ChatFragment.adapter
                }
                messageRcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        isAtBottom =
                            manager.findLastCompletelyVisibleItemPosition() >= manager.itemCount - HIDDEN_AMOUNT
                        if (isAtBottom) {
                            observer.unSeenMessages = 0
                            showNewMessage = false
                            tvNewMessage.visibility = View.GONE
                            hideBottomScroll()
                        } else {
                            showBottomScroll()
                        }
                    }
                })
                setupSwipeToReply()
                viewModel.startListening()
            }
        }
        viewModel.listMessage.observe(viewLifecycleOwner) {
            if (viewModel.listMember.value == null)
                return@observe
            messages = it
            adapter.submitList(it)
            if (firstLoad) {
                firstLoad = false
                messageRcv.scrollToPosition(adapter.itemCount - 1)
            }
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViews() {
        quoteLayout = binding.inputBar.findViewById(R.id.item_message_quote)
        messageRcv = binding.rcvMessages
        bottomScroll = binding.bottomScroll
        tvNewMessage = binding.tvNewMessage

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
                EditorInfo.IME_ACTION_SEND -> sendMessage()
            }
            true
        }

        binding.btnSend.setOnClickListener {
            if (!isAtBottom) messageRcv.scrollToPosition(adapter.itemCount - 1)
            sendMessage()
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
                        sendImagesMessage(result)
                    }

                    override fun onCancel() {}
                })
        }

        binding.btnMore.setOnClickListener {
            showMenu(it, R.menu.popup_menu_chat_more)
        }

        bottomScroll.setOnClickListener {
            messages.let {
                messageRcv.scrollToPosition(it.size - 1)
            }
        }

        quoteLayout.cancelReplyButton.setOnClickListener {
            hideReplyLayout()
        }
    }

    private fun hideBottomScroll() {
        bottomScroll.animate().setDuration(50).translationY(50f).alpha(0f).withEndAction {
            bottomScroll.visibility = View.GONE
        }
        tvNewMessage.animate().setDuration(50).translationY(50f).alpha(0f).withEndAction {
            bottomScroll.visibility = View.GONE
        }
    }

    private fun showBottomScroll() {
        bottomScroll.visibility = View.VISIBLE
        bottomScroll.animate().setDuration(100).translationY(0f).alpha(1f)
    }

    private fun showNewMessage() {
        tvNewMessage.visibility = View.VISIBLE
        tvNewMessage.animate().setDuration(100).translationY(0f).alpha(1f)
    }


    private fun sendImagesMessage(result: ArrayList<LocalMedia>) {
        val idle = viewModel.checkIdle()
        val mediaList = result.map { it.toChatMedia() }
        val message = Message(
            id = Util.autoId(),
            sender = currentUserId,
            idleBreak = idle,
            medias = mediaList
        )
        if (showingQuote && mQuoted != null) {
            message.quotedMessage = mQuoted!!.id
            hideReplyLayout()
        }
        viewModel.sendImagesMessage(requireContext(), result, message)
    }

    private fun sendMessage() {
        val idle = viewModel.checkIdle()
        val message = createMessage()
        message.apply { this.idleBreak = idle }
        viewModel.sendMessage(message)
        binding.input.setText("")
    }

    private fun createMessage(): Message {
        val text = binding.input.text.toString().trim()
        val message = Message(
            id = Util.autoId(),
            sender = currentUserId,
            text = text.ifEmpty { null }
        )
        if (text.isEmpty())
            message.apply { isReact = true }
        if (showingQuote && mQuoted != null) {
            message.quotedMessage = mQuoted!!.id
            hideReplyLayout()
        }

        return message
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
        const val HIDDEN_AMOUNT = 6

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

    private fun setupSwipeToReply() {
        val messageSwipeController = MessageSwipeCallback(
            requireContext(),
            object : MessageSwipeActions {
                override fun showReplyUI(position: Int) {
                    val chatMessage = adapter.currentList[position]
                    showQuotedMessage(chatMessage)
                }
            }
        )

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        try {
            itemTouchHelper.attachToRecyclerView(messageRcv)
        } catch (npe: NullPointerException) {
            Log.i(TAG, "UI already teared down", npe)
        }
    }

    private fun showQuotedMessage(message: Message) {
        binding.input.requestFocus()
        val inputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.input, InputMethodManager.SHOW_IMPLICIT)
        quoteLayout.visibility = View.VISIBLE
        quoteLayout.animate().setDuration(200).translationY(0f)
        quoteLayout.cancelReplyButton.visibility = View.VISIBLE


        quoteLayout.quotedMessage.apply {
            isVisible = message.text != null
            text = message.text
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        quoteLayout.quoteColoredView.isVisible = true
        if (message.medias.isNullOrEmpty()) {
            quoteLayout.quotedMessageImage.visibility = View.GONE
        } else
            quoteLayout.quotedMessageImage.apply {
                val source = message.medias!![0].path
                visibility = View.VISIBLE
                setImageUrl(source)
            }

        val replyTo =
            if (message.sender == currentUserId)
                getString(R.string.yourself)
            else mapMember[message.sender]!!.name.split(' ').last()
        quoteLayout.quotedMessageAuthor.text = getString(R.string.reply_to, replyTo)
        showingQuote = true
        mQuoted = message
    }

    private fun hideReplyLayout() {
        showingQuote = false
        mQuoted = null
        quoteLayout.animate().setDuration(200).translationY(quoteLayout.height.toFloat()).withEndAction {
            quoteLayout.visibility = View.GONE
        }
    }
}

interface ChatListener {
    fun openPreview(position: Int, data: List<ChatMedia>)
}