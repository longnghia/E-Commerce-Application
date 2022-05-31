package com.goldenowl.ecommerce.ui.global.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemCategoryListBinding

class AppBarCategoryListAdapter(private val listCategory: List<String>, private val callback: IClickListener) :
    RecyclerView.Adapter<AppBarCategoryListAdapter.CategoryViewHolder>() {


    interface IClickListener {
        fun onClick(position: Int)
    }


    inner class CategoryViewHolder(val binding: ItemCategoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String, position: Int, iClickListener: IClickListener) {
            binding.btnCategory.text = category
            binding.btnCategory.setOnClickListener {
                iClickListener.onClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = listCategory[position]
        holder.bind(category, position, callback)
    }

    override fun getItemCount(): Int {
        if (listCategory == null) return 0
        return listCategory.size
    }
}
