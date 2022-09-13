package com.ln.simplechat.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatItemBinding
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.ChannelAndMember
import com.ln.simplechat.utils.setImageUrl
import io.getstream.avatarview.coil.loadImage

class ChannelAdapter(private val currentUserId: String?, private val onClickChannel: (Channel) -> Unit) :
    ListAdapter<ChannelAndMember, ContactViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val channelAndMember = getItem(position)
        val (channel, listMember) = channelAndMember
        val others = listMember.filter { member -> member.id != currentUserId }
        channel.icon?.let {
            holder.binding.icon.setImageUrl(it)
        }
        holder.binding.name.text = others[0].name
        holder.itemView.setOnClickListener {
            onClickChannel(channel)
        }

        holder.binding.icon.loadImage(
            data = others.map { it.avatar }
        )
    }
}

class ContactViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
) {
    val binding: ChatItemBinding = ChatItemBinding.bind(itemView)
}


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChannelAndMember>() {
    override fun areItemsTheSame(oldItem: ChannelAndMember, newItem: ChannelAndMember): Boolean {
        return oldItem.channel.id == newItem.channel.id
    }

    override fun areContentsTheSame(oldItem: ChannelAndMember, newItem: ChannelAndMember): Boolean {
        return oldItem == newItem
    }
}