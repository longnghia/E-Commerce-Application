package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemBottomSheetWriteReviewBinding
import com.goldenowl.ecommerce.databinding.ItemBottomSheetWriteReviewCameraBinding
import com.goldenowl.ecommerce.utils.Utils

class BottomSheetWriteReviewAdapter(private val addImageListener: () -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listImage: List<String> = emptyList()

    fun setData(list: List<String>) {
        listImage = list
        notifyDataSetChanged()
    }

    class ViewHolderImage(private val binding: ItemBottomSheetWriteReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            Utils.glide2View(binding.ivImg, binding.layoutLoading.loadingFrameLayout, url)
        }
    }

    inner class ViewHolderFooter(val binding: ItemBottomSheetWriteReviewCameraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                if (listImage.size < 5)
                    addImageListener.invoke()
                else
                    Toast.makeText(
                        binding.root.context,
                        binding.root.context.getString(R.string.maximum_5_pic),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewBinding
        if (viewType == TYPE_ITEM) {
            binding =
                ItemBottomSheetWriteReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderImage(binding)
        } else if (viewType == TYPE_FOOTER) {
            binding =
                ItemBottomSheetWriteReviewCameraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderFooter(binding)
        }
        throw RuntimeException("There is no type that matches the type $viewType. Make sure you are using view types correctly!")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_ITEM -> (holder as ViewHolderImage).bind(listImage[position])
            TYPE_FOOTER -> (holder as ViewHolderFooter).bind()
        }
    }

    override fun getItemCount(): Int = listImage.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == listImage.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 0
    }
}