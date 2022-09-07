package com.ln.simplechat.ui.chat

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ItemReactBinding
import com.ln.simplechat.databinding.ItemTimelineBinding
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.model.MessageState
import com.ln.simplechat.model.MessageType
import com.ln.simplechat.ui.chat.swipe.MessageSwipeCallback
import com.ln.simplechat.utils.DateUtils
import com.ln.simplechat.utils.setImageUrl
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.utils.DensityUtil
import kotlinx.android.synthetic.main.item_message_quote.view.*
import java.util.*


class ChatAdapter(
    private val context: Context,
    private val currentUserId: String,
    private val mapMember: Map<String, Member>,
    private val chatListener: ChatListener
) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val imageSpacing = context.resources.getDimension(R.dimen.image_spacing)
    private val imageSize = context.resources.getDimension(R.dimen.image_size)

    private val tint = object {
        val incomingQuote: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.colorGrayLight)
        )
        val outgoingQuote: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.colorBlueLight)
        )
    }

    private val padding = object {
        val quote = context.resources.getDimension(R.dimen.quote_padding).toInt()
    }

    private val margin = object {
        val quoteBottom = -context.resources.getDimension(R.dimen.bubble_radius_round).toInt()
    }

    var onMessageLongClickListener: ((View, String) -> Unit)? = null
    var onQuoteClickListener: ((Int) -> Unit)? = null

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
        var reactionRcv: RecyclerView = itemView.findViewById(R.id.rcv_reaction)
        var reactionCount: TextView = itemView.findViewById(R.id.reaction_count)
        private val reactLayout: LinearLayout = itemView.findViewById(R.id.layout_react)
        private val quoteLayout: LinearLayout = itemView.findViewById(R.id.item_message_quote)

        fun bind(
            item: Message,
            index: Int,
            nextItem: Message?,
            beforeItem: Message?,
        ) {
            val outGoing = item.sender == currentUserId
            itemView.setTag(MessageSwipeCallback.SWIPE_TO_LEFT, outGoing)
            /* UI message background top-mid-bot */
            val layout = mapOf(
                MessageState.TOP to if (outGoing) R.drawable.message_outgoing_top else R.drawable.message_incoming_top,
                MessageState.MIDDLE to if (outGoing) R.drawable.message_outgoing_mid else R.drawable.message_incoming_mid,
                MessageState.BOTTOM to if (outGoing) R.drawable.message_outgoing_bot else R.drawable.message_incoming_bot,
                MessageState.NORMAL to if (outGoing) R.drawable.message_outgoing else R.drawable.message_incoming,
            )
            val below =
                if (index == 0 || beforeItem!!.isTimeline || item.idleBreak || item.isReact || beforeItem.isReact || item.quotedMessage != null)
                    false
                else item.sender == beforeItem.sender
            val upper =
                if (index == itemCount - 1 || nextItem!!.isTimeline || nextItem.idleBreak || nextItem.isReact || nextItem.quotedMessage != null)
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
                    /* on long click reaction */
                    messageText.setOnLongClickListener { view ->
                        Log.d(TAG, "onBindViewHolder:setOnLongClickListener=$view ")
                        onMessageLongClickListener?.invoke(view, item.id)
                        true
                    }
                }
                MessageType.MEDIA -> {
                    messageText.visibility = View.GONE
                    messageImageRcv.visibility = View.VISIBLE
                    loadImages(messageImageRcv, outGoing, item, onMessageLongClickListener)
                }
            }
            timestamp.text = DateUtils.SDF_HOUR.format(item.timestamp)
            timestamp.isVisible = nextItem?.idleBreak ?: true

            if (outGoing) {
                messenger.visibility = View.GONE
                messengerAvatar.visibility = View.GONE
            } else {
                val user = mapMember[item.sender]!!
                messengerAvatar.isVisible = state == MessageState.BOTTOM || state == MessageState.NORMAL
                if (messengerAvatar.isVisible) loadImageIntoView(messengerAvatar, user.avatar)
                messenger.isVisible =
                    (state == MessageState.TOP || state == MessageState.NORMAL) && item.quotedMessage == null
                messenger.text = user.name
            }

            val data = item.reactions.getNonEmptyReaction()
            if (data.isEmpty())
                reactLayout.visibility = View.GONE
            else {
                reactLayout.visibility = View.VISIBLE
                reactionRcv.adapter = ReactionAdapter(data)
                if (data.size > 1)
                    reactionCount.apply {
                        visibility = View.VISIBLE
                        val count = data.sumOf { it.second.size }
                        text = count.toString()
                    }
                else
                    reactionCount.visibility = View.GONE
            }
            quoteLayout.isVisible = item.quotedMessage != null
            if (item.quotedMessage != null)
                bindQuote(item, outGoing)
        }

        private fun bindQuote(item: Message, outGoing: Boolean) {
            val quoteId = item.quotedMessage
            val index = currentList.indexOfFirst { it.id == quoteId }
            val quotedMessage = currentList[index]
            quoteLayout.apply {
                updateLayoutParams<LinearLayout.LayoutParams> {
                    bottomMargin = margin.quoteBottom
                }
                gravity = Gravity.END
            }
            quoteLayout.flexboxQuotedContent.apply {
                setPadding(padding.quote, padding.quote / 2, padding.quote, padding.quote) //padding
            }
            if (outGoing) {
                quoteLayout.apply {
                    gravity = Gravity.END
                }
                quoteLayout.flexboxQuoted.gravity = Gravity.END
                quoteLayout.flexboxQuotedContent.apply {
                    setBackgroundResource(R.drawable.message_outgoing_top)
                    backgroundTintList = tint.outgoingQuote
                    gravity = Gravity.END
                }
            } else {
                quoteLayout.apply {
                    gravity = Gravity.START
                }
                quoteLayout.flexboxQuoted.gravity = Gravity.START
                quoteLayout.flexboxQuotedContent.apply {
                    setBackgroundResource(R.drawable.message_incoming_top)
                    backgroundTintList = tint.incomingQuote
                    gravity = Gravity.START
                }
            }
            quoteLayout.setOnClickListener {
                onQuoteClickListener?.invoke(index)
            }
            quoteLayout.quotedMessageAuthor.text = getRepliedName(context, item, quotedMessage)

            quoteLayout.quotedMessage.apply {
                isVisible = quotedMessage.text != null
                text = quotedMessage?.text
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            }

            if (quotedMessage.medias.isNullOrEmpty()) {
                quoteLayout.quotedMessageImage.visibility = View.GONE
            } else
                quoteLayout.quotedMessageImage.apply {
                    visibility = View.VISIBLE
                    val source = quotedMessage.medias!![0].path
                    setImageUrl(source)
                }
        }
    }

    fun getRepliedName(context: Context, currentMessage: Message, quotedMessage: Message): String {
        val quotedSender = mapMember[quotedMessage.sender]!!
        val messageSender = mapMember[currentMessage.sender]!!
        val repliedName: String
        val replierName: String
        if (currentMessage.sender == currentUserId) {
            replierName = context.getString(R.string.you)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            repliedName = if (quotedSender.id == currentUserId)
                context.getString(R.string.yourself)
            else
                quotedSender.name.split(' ').last()
        } else {
            replierName = messageSender.name.split(' ').last()
            repliedName = if (quotedSender.id == currentUserId)
                context.getString(R.string.you)
            else if (currentMessage.sender == quotedMessage.sender)
                context.getString(R.string.himself)
            else
                quotedSender.name.split(' ').last()
        }
        return context.getString(R.string.who_replied_to_who, replierName, repliedName)
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

    private fun loadImages(
        rcv: RecyclerView,
        outGoing: Boolean = false,
        item: Message,
        onMessageLongClickListener: ((View, String) -> Unit)?
    ) {
        val urls = item.medias
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
        onMessageLongClickListener?.let {
            adapter.setOnItemLongClickListener { view ->
                Log.d(TAG, "loadImages: setOnItemLongClickListener=$view")
                it.invoke(view, item.id)
            }
        }
        rcv.adapter = adapter
    }

    private fun loadImageIntoView(view: ImageView, url: String) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    view.setImageUrl(downloadUrl)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "loadImageIntoView: ERROR", e)
                }
        } else {
            view.setImageUrl(url)
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




