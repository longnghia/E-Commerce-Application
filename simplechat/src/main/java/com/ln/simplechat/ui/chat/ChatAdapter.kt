package com.ln.simplechat.ui.chat

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ln.simplechat.R
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.utils.DensityUtil
import java.text.SimpleDateFormat

class ChatAdapter(
    private val context: Context,
    private val currentUserId: String,
    private val listUser: List<Member>,
    private val chatListener: ChatListener
) :
    ListAdapter<Message, ChatAdapter.MessageViewHolder>(DIFF_CALLBACK) {

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
        holder.bind(model)
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
        var messageVideo: VideoView = itemView.findViewById(R.id.message_video)
        var timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(item: Message) {
            toggleVisibility(item)
            if (item.text != null) {
                messageText.text = item.text
                setTextColor(item.sender, messageText)
            } else if (item.listImageUrl != null) {
                loadImages(messageImageRcv, item.listImageUrl)
            } else if (item.videoUrl != null) {

            }
            timestamp.text = dateFormatter.format(item.timestamp)
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

        private fun toggleVisibility(item: Message) {
            if (item.text != null) {
                messageText.visibility = View.VISIBLE
                messageImageRcv.visibility = View.GONE
                messageVideo.visibility = View.GONE
            } else if (item.listImageUrl != null) {
                messageText.visibility = View.GONE
                messageImageRcv.visibility = View.VISIBLE
                messageVideo.visibility = View.GONE
            } else if (item.videoUrl != null) {
                messageText.visibility = View.GONE
                messageImageRcv.visibility = View.GONE
                messageVideo.visibility = View.VISIBLE
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

    private fun loadImages(rcv: RecyclerView, urls: List<ChatMedia>?) {
        if (urls.isNullOrEmpty()) return
        val spanCount = when (urls.size) {
            1 -> 1
            2, 4 -> 2
            else -> 3
        }
        val manager = GridLayoutManager(context, spanCount)
        rcv.layoutManager = manager
        val itemAnimator: RecyclerView.ItemAnimator? = rcv.itemAnimator
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        rcv.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount,
                DensityUtil.dip2px(context, 1F), false
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
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }

        val dateFormatter = SimpleDateFormat("MMMM dd")
    }

    interface ChatListener {
        fun openPreview(position: Int, data: List<ChatMedia>)
    }
}




