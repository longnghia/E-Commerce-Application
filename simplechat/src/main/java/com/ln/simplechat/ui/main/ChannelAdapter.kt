package com.ln.simplechat.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ln.simplechat.R
import com.ln.simplechat.databinding.ChatItemBinding
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.ChannelAndMember
import io.getstream.avatarview.coil.loadImage

class ChannelAdapter(private val onClickChannel: (Channel) -> Unit) :
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
        Glide.with(holder.itemView.context).load(channel.icon).into(holder.binding.icon)
        holder.binding.name.text = channel.name
        holder.itemView.setOnClickListener {
            onClickChannel(channel)
        }
        holder.binding.icon.loadImage(
            data = listMember.map { it.avatar }
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