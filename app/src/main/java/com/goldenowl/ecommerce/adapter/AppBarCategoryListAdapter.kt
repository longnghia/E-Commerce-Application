package com.goldenowl.ecommerce.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemAppBarCategoryListBinding

class AppBarCategoryListAdapter(
    private val listCategory: List<String>,
    private val callback: IClickListener
) :
    RecyclerView.Adapter<AppBarCategoryListAdapter.CategoryViewHolder>() {

    private var checkedPosition = -1

    fun setPosition(position: Int) {
        checkedPosition = position
        notifyDataSetChanged()
    }

    interface IClickListener {
        fun onClick(position: Int)
    }

    inner class CategoryViewHolder(val binding: ItemAppBarCategoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("RestrictedApi")
        fun bind(category: String, position: Int, iClickListener: IClickListener) {
            binding.btnCategory.text = category
            binding.btnCategory.setOnClickListener {
                iClickListener.onClick(position)
                Log.d("Appbar", "bind: clicked $position")
                setPosition(position)
            }

            if (position == checkedPosition) {
                binding.btnCategory.apply {
                    supportBackgroundTintList = this.resources.getColorStateList(R.color.red_dark)
                }
            } else {
                binding.btnCategory.apply {
                    supportBackgroundTintList = this.resources.getColorStateList(R.color.black_light)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemAppBarCategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
