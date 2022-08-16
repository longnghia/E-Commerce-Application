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

class ChannelAdapter(private val onClickChannel: (Channel) -> Unit) :
    ListAdapter<Channel, ContactViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val channel = getItem(position)
        Glide.with(holder.itemView.context).load(channel.icon).into(holder.binding.icon)
        holder.binding.name.text = channel.name
        holder.itemView.setOnClickListener {
            onClickChannel(channel)
        }
    }
}

class ContactViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
) {
    val binding: ChatItemBinding = ChatItemBinding.bind(itemView)
}


private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Channel>() {
    override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
        return oldItem == newItem
    }
}