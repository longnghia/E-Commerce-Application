package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R

class ModalAdapter : RecyclerView.Adapter<ModalAdapter.ViewHolder>() {
    private val listSize = listOf("L", "M", "S", "XL", "XS")
    private var checkItem = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tv: TextView? = null

        init {
            tv = view.findViewById(R.id.tv_size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_sheet_size, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = listSize[position]
        holder.tv?.apply {
            this.text = text
            this.setOnClickListener {
                refresh(position)
            }
            if (position == checkItem) {
                this.isSelected = true
                this.setTextColor(R.color.white)
            } else {
                this.isSelected = false
                this.setTextColor(R.color.black_light)
            }
        }

    }

    private fun refresh(position: Int) {
        checkItem = position
        notifyDataSetChanged()
    }

    fun getCheckedSize() = listSize[checkItem]

    override fun getItemCount() = listSize.size

    companion object {
        val TAG = "ModalAdapter"
    }
}
