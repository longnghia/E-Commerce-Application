package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.LayoutHomeViewPagerImageBinding

class HomeViewPagerAdapter(private val imagesList: List<Int>, private val titleList: List<String>) :
    RecyclerView.Adapter<HomeViewPagerAdapter.ViewHolder>() {

    class ViewHolder(val binding: LayoutHomeViewPagerImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: Int, title: String) {
            binding.productImg.setImageResource(url)
            binding.tvTitle.text = title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutHomeViewPagerImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = imagesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = imagesList[position]
        val title = titleList[position]
        holder.bind(url, title)
    }

    companion object {
        val TAG = "ImageProductViewpager"
    }
}