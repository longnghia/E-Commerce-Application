package com.goldenowl.ecommerce.ui.global.home

import android.app.Activity
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.goldenowl.ecommerce.databinding.ItemProductListHomeBinding
import com.goldenowl.ecommerce.models.data.Product
import kotlinx.android.synthetic.main.item_product_list_home.view.*


class HomeProductListAdapter(private val productList: List<Product>, private val isGrid: Boolean) :
    RecyclerView.Adapter<HomeProductListAdapter.ProductViewHolder>() {
    class ProductViewHolder(private val binding: ItemProductListHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.productName.text = product.title
            binding.productBrand.text = product.brandName
            product.getImage().let {
                if (it != null) {
                    Glide
                        .with(binding.productImg.context)
                        .load(it)
                        .listener(glideListener(binding.layoutLoading.loadingFrameLayout))
                        .into(binding.productImg)
                }
            }
            binding.layoutRating.product_rating_bar.rating = product.reviewStars.toFloat()
            product.salePercent.let {
                if (it != null) {
                    binding.tvProductDiscount.visibility = View.VISIBLE
                    binding.productCurrentPrice.text =
                        ((product.getOriginPrice()?.times(100 - it) ?: 0) / 100).toFloat().toString() + "$"
                    binding.productOldPrice.apply {
                        text = product.getOriginPrice().toString() + "$"
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                } else {
                    binding.tvProductDiscount.visibility = View.INVISIBLE
                    binding.productOldPrice.text = product.getOriginPrice().toString() + "$"
                    binding.productCurrentPrice.visibility = View.INVISIBLE
                }
            }
            binding.tvRateCount.text = product.numberReviews.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemBinding = ItemProductListHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        Log.d("onBindViewHolder", "onBindViewHolder")
        val product = productList[position]
        holder.bind(product)
        if(isGrid){
            val displaymetrics = DisplayMetrics()
            (holder.itemView.context as Activity).windowManager.defaultDisplay.getMetrics(displaymetrics)

            val devicewidth = displaymetrics.widthPixels / 2
            holder.itemView.product_img.getLayoutParams().width = devicewidth
        }
    }

    override fun getItemCount() = productList.size

    companion object {
        fun glideListener(loadingLayout: View): RequestListener<Drawable> {
            return object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.w("Glide", "onLoadFailed: ", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadingLayout.visibility = View.GONE
                    return false
                }

            }
        }
    }
}