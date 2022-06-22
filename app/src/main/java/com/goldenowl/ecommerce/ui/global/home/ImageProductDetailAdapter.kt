package com.goldenowl.ecommerce.ui.global.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.utils.Utils

class ImageProductDetailAdapter(private val imagesList: List<String>) :
    RecyclerView.Adapter<ImageProductDetailAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.product_img)
        val layoutLoading: FrameLayout = view.findViewById(R.id.layout_loading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_product_image, parent, false))
    }

    override fun getItemCount(): Int = imagesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = imagesList[position]
        Utils.glide2View(holder.productImage, holder.layoutLoading, url)
    }

    companion object {
        val TAG = "ImageProductViewpager"
    }
}