package com.goldenowl.ecommerce.ui.global.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemCategoryBinding

class CategoryAdapter(private val listCategory: List<String>, private val listener: (String) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: String) {

            binding.tvCategory.apply {
                text = history
                setOnClickListener {
                    listener(history)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = listCategory.elementAt(position)
        holder.bind(history)
    }

    override fun getItemCount() = if (listCategory.size < ITEM_COUNT) listCategory.size else ITEM_COUNT

    companion object {
        const val ITEM_COUNT = 6
    }
}
