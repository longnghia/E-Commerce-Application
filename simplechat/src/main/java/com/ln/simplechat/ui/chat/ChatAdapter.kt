package com.ln.simplechat.ui.chat

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.model.MessageType
import com.ln.simplechat.utils.DateUtils
import com.ln.simplechat.utils.DateUtils.isSameDate
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.utils.DensityUtil

class ChatAdapter(
    private val context: Context,
    private val currentUserId: String,
    private val listUser: List<Member>,
    private val chatListener: ChatListener
) :
    ListAdapter<Message, ChatAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    private val scale = context.resources.displayMetrics.density
    private val imageSpacing = context.resources.getDimension(R.dimen.image_spacing)
    private val imageSize = context.resources.getDimension(R.dimen.image_size)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            MESSAGE_OUTGOING -> {
                val view = inflater.inflate(R.layout.item_message_outgoing, parent, false)
                MessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_message_incomming, parent, false)
                MessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val model = getItem(position)
        var nextModel: Message? = if (position + 1 >= itemCount) null else getItem(position + 1)
        val beforeModel: Message? = if (position - 1 > 0) getItem(position - 1) else null

        holder.bind(model, nextModel, beforeModel)
    }

    override fun getItemViewType(position: Int): Int {
        return getType(getItem(position))
    }

    private fun getType(model: Message): Int {
        return if (model.sender == currentUserId) MESSAGE_OUTGOING else MESSAGE_INCOMING
    }

    inner class MessageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView = itemView.findViewById(R.id.message_text)
        var messenger: TextView = itemView.findViewById(R.id.messenger)
        var messengerAvatar: ImageView = itemView.findViewById(R.id.messenger_avatar)
        var messageImageRcv: RecyclerView = itemView.findViewById(R.id.rcv_message_image)
        var timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        var layoutTimeline: ConstraintLayout = itemView.findViewById(R.id.layout_timestamp)
        var timeline: TextView = itemView.findViewById(R.id.timeline)
        fun bind(
            item: Message,
            nextItem: Message?,
            beforeItem: Message?,
        ) {
            var isDayAfter = beforeItem?.let { isSameDate(it.timestamp, item.timestamp) } ?: false
            var isDayBefore = nextItem?.let { isSameDate(it.timestamp, item.timestamp) } ?: false
            /* if item is different date, show timeline header */
            if (isDayAfter) {
                layoutTimeline.visibility = View.GONE
            } else {
                layoutTimeline.visibility = View.VISIBLE
                val date = DateUtils.getTimeline(context, item.timestamp)
                timeline.text = "${DateUtils.SDF_HOUR.format(item.timestamp)} $date"
            }

            val outGoing = item.sender == currentUserId
            when (item.getMessageType()) {
                MessageType.TEXT -> {
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
            /* if next message is from the same sender, hide timestamp, show on item long clicked */
            var nextMessengerSame = nextItem?.sender == item.sender
            timestamp.visibility = if (nextMessengerSame && isDayBefore) View.GONE else View.VISIBLE

            val user = listUser.find { it.id == item.sender }
            if (user != null) {
                messenger.text = user.name
                if (user.id == currentUserId)
                    messengerAvatar.visibility = View.GONE
                else
                    loadImageIntoView(messengerAvatar, user.avatar)
            } else {
                messenger.text = item.sender
                messengerAvatar.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }

        private fun setTextColor(userId: String?, textView: TextView) {
            if (currentUserId == userId) {
                textView.setTextColor(Color.WHITE)
            } else {
                textView.setTextColor(Color.BLACK)
            }
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




