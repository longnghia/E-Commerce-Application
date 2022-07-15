package com.goldenowl.ecommerce.ui.global.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemHistoryBinding

class HistoryAdapter(historyList: List<String>, private val listener: (String) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var mHistoryList = historyList

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: String) {

            binding.tvHistory.apply {
                text = history
                setOnClickListener {
                    listener(history)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = mHistoryList.elementAt(position)
        holder.bind(history)
    }

    override fun getItemCount() = mHistoryList.size

    fun refresh(newList: List<String>) {
        mHistoryList = newList
        notifyItemInserted(0)
    }
}
