package com.ln.simplechat.ui.chat

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ItemReactBinding
import com.ln.simplechat.databinding.ItemTimelineBinding
import com.ln.simplechat.model.*
import com.ln.simplechat.utils.DateUtils
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.utils.DensityUtil


class ChatAdapter(
    private val context: Context,
    private val currentUserId: String,
    private val mapMember: Map<String, Member>,
    private val chatListener: ChatListener
) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val imageSpacing = context.resources.getDimension(R.dimen.image_spacing)
    private val imageSize = context.resources.getDimension(R.dimen.image_size)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            MESSAGE_OUTGOING -> {
                val view = inflater.inflate(R.layout.item_message_outgoing, parent, false)
                MessageViewHolder(view)
            }
            MESSAGE_INCOMING -> {
                val view = inflater.inflate(R.layout.item_message_incomming, parent, false)
                MessageViewHolder(view)
            }
            MESSAGE_REACT -> {
                ReactViewHolder(ItemReactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else -> {
                val view = inflater.inflate(R.layout.item_timeline, parent, false)
                TimelineViewHolder(ItemTimelineBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = getItem(position)
        var nextModel: Message? = if (position + 1 >= itemCount) null else getItem(position + 1)
        val beforeModel: Message? = if (position - 1 >= 0) getItem(position - 1) else null
        when (holder) {
            is MessageViewHolder ->
                holder.bind(model, position, nextModel, beforeModel)
            is TimelineViewHolder -> holder.bind(model)
            is ReactViewHolder -> holder.bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item.isReact)
            return MESSAGE_REACT
        if (item.isTimeline)
            return MESSAGE_TIMELINE
        return if (item.sender == currentUserId) MESSAGE_OUTGOING else MESSAGE_INCOMING
    }

    inner class MessageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById(R.id.message_text)
        var messenger: TextView = itemView.findViewById(R.id.messenger)
        var messengerAvatar: ImageView = itemView.findViewById(R.id.messenger_avatar)
        var messageImageRcv: RecyclerView = itemView.findViewById(R.id.rcv_message_image)
        var timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(
            item: Message,
            index: Int,
            nextItem: Message?,
            beforeItem: Message?,
        ) {
            val outGoing = item.sender == currentUserId

            /* UI message background top-mid-bot */
            val layout = mapOf(
                MessageState.TOP to if (outGoing) R.drawable.message_outgoing_top else R.drawable.message_incoming_top,
                MessageState.MIDDLE to if (outGoing) R.drawable.message_outgoing_mid else R.drawable.message_incoming_mid,
                MessageState.BOTTOM to if (outGoing) R.drawable.message_outgoing_bot else R.drawable.message_incoming_bot,
                MessageState.NORMAL to if (outGoing) R.drawable.message_outgoing else R.drawable.message_incoming,
            )
            val below =
                if (index == 0 || beforeItem!!.isTimeline || item.idleBreak || item.isReact || beforeItem.isReact)
                    false
                else item.sender == beforeItem.sender
            val upper =
                if (index == itemCount - 1 || nextItem!!.isTimeline || nextItem.idleBreak || nextItem.isReact)
                    false
                else item.sender == nextItem.sender

            val state =
                if (upper)
                    if (below) MessageState.MIDDLE else MessageState.TOP
                else
                    if (below) MessageState.BOTTOM else MessageState.NORMAL

            when (item.getMessageType()) {
                MessageType.TEXT -> {
                    messageText.setBackgroundResource(layout[state]!!)
                    messageText.visibility = View.VISIBLE
                    messageImageRcv.visibility = View.GONE
                    messageText.text = item.text
                    setTextColor(item.sender, messageText)
                }
                MessageType.MEDIA -> {
                    messageText.visibility = View.GONE
                    messageImageRcv.visibility = View.VISIBLE
                    loadImages(messageImageRcv, outGoing, item.medias)
                }
            }
            timestamp.text = DateUtils.SDF_HOUR.format(item.timestamp)
            timestamp.isVisible = state == MessageState.BOTTOM || state == MessageState.NORMAL

            if (outGoing) {
                messenger.visibility = View.GONE
                messengerAvatar.visibility = View.GONE
            } else {
                val user = mapMember[item.sender]!!
                messengerAvatar.isVisible = state == MessageState.BOTTOM || state == MessageState.NORMAL
                if (messengerAvatar.isVisible) loadImageIntoView(messengerAvatar, user.avatar)
                messenger.isVisible = state == MessageState.TOP || state == MessageState.NORMAL
                messenger.text = user.name
            }
        }
    }

    private fun setTextColor(userId: String?, textView: TextView) {
        if (currentUserId == userId) {
            textView.setTextColor(Color.WHITE)
        } else {
            textView.setTextColor(Color.BLACK)
        }
    }

    inner class ReactViewHolder(binding: ItemReactBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.ivReact
        private val chatMargin = context.resources.getDimensionPixelSize(R.dimen.chat_margin)
        fun bind(item: Message) {
            val outGoing = item.sender == currentUserId
            val layoutParams = image.layoutParams as FrameLayout.LayoutParams
            if (outGoing) {
                image.layoutParams = layoutParams.apply {
                    setMargins(chatMargin)
                    gravity = Gravity.END
                }
            } else {
                image.layoutParams = layoutParams.apply {
                    setMargins(chatMargin)
                    gravity = Gravity.START
                }
            }


            image.setImageResource(R.drawable.ic_like)
        }
    }

    class TimelineViewHolder(
        private val binding: ItemTimelineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            val date = DateUtils.getTimeline(itemView.context, item.timestamp)
            binding.timeline.text = "${DateUtils.SDF_HOUR.format(item.timestamp)} $date"
        }
    }

    private fun loadImages(rcv: RecyclerView, outGoing: Boolean = false, urls: List<ChatMedia>?) {
        if (urls.isNullOrEmpty()) return
        val spanCount = when (urls.size) {
            1 -> 1
            2, 4 -> 2
            else -> 3
        }
        val flexWidth = spanCount * (imageSize + imageSpacing + 5) // extra pixel due to pixel conversion
        rcv.layoutParams = rcv.layoutParams.apply { width = flexWidth.toInt() }

        val manager = FlexboxLayoutManager(context)
        if (outGoing)
            manager.justifyContent = JustifyContent.FLEX_END

        rcv.layoutManager = manager

        val itemAnimator: RecyclerView.ItemAnimator? = rcv.itemAnimator
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        rcv.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount,
                DensityUtil.dip2px(context, imageSpacing), false
            )
        )
        val adapter = GridImageAdapter(context, urls)
        adapter.setOnItemClickListener { _, position ->
            chatListener.openPreview(position, urls)
        }

        rcv.adapter = adapter
    }

    private fun loadImageIntoView(view: ImageView, url: String) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Glide.with(context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "loadImageIntoView: ERROR", e)
                }
        } else {
            Glide.with(context).load(url).into(view)
        }
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val MESSAGE_INCOMING = 0
        const val MESSAGE_OUTGOING = 1
        const val MESSAGE_TIMELINE = 2
        const val MESSAGE_REACT = 3

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }
}




