package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.databinding.ItemBottomSheetWriteReviewBinding
import com.goldenowl.ecommerce.utils.Utils

class ReviewImagesAdapter(private val listImage: List<String>) :
    RecyclerView.Adapter<ReviewImagesAdapter.ViewHolderImage>() {

    class ViewHolderImage(private val binding: ItemBottomSheetWriteReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Utils.glide2View(binding.ivImg, binding.layoutLoading.loadingFrameLayout, url)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderImage {
        val binding: ViewBinding
        binding =
            ItemBottomSheetWriteReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderImage(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderImage, position: Int) {
        holder.bind(listImage[position])
    }

    override fun getItemCount(): Int = listImage.size
}