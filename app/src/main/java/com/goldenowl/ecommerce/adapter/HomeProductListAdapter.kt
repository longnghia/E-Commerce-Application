package com.goldenowl.ecommerce.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.utils.Utils.glideListener
import com.goldenowl.ecommerce.utils.Utils.strike
import java.util.*


class HomeProductListAdapter(private val listener: IClickListener) :
    RecyclerView.Adapter<HomeProductListAdapter.HomeProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>, filterType: String) {
        mListProductData = listProductData
        if (filterType == "Sales")
            mListProductData = mListProductData.filter { it.product.salePercent != null }
        else if (filterType == "News")
            mListProductData = mListProductData.filter { it.product.createdDate > Date(0) }
        Log.d(TAG, "setData: listProductData = $mListProductData")
        notifyDataSetChanged()
    }

    class HomeProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var productName: TextView? = null
        var productBrand: TextView? = null
        var tvColor: TextView? = null
        var tvDiscountPercent: TextView? = null
        var tvSize: TextView? = null
        var originPrice: TextView? = null
        var discountPrice: TextView? = null
        var tvNumberReviews: TextView? = null
        var productImg: ImageView? = null
        var productRatingBar: RatingBar? = null
        var ivFavorite: ImageView? = null
        var layoutLoading: FrameLayout? = null
        var layoutFrameLoading: FrameLayout? = null

        init {
            productName = itemView.findViewById(R.id.product_name)
            productBrand = itemView.findViewById(R.id.product_brand)
            productImg = itemView.findViewById(R.id.product_img)
            productRatingBar = itemView.findViewById(R.id.product_rating_bar)
            tvNumberReviews = itemView.findViewById(R.id.tv_number_reviews)
            ivFavorite = itemView.findViewById(R.id.iv_favorite)
            layoutLoading = itemView.findViewById(R.id.layout_loading)
            if (layoutLoading != null) {
                layoutFrameLoading = layoutLoading!!.findViewById(R.id.loading_frame_layout) ?: null
            } else {
                Log.d(TAG, "layoutLoading NULL!! ")
            }
            tvColor = itemView.findViewById(R.id.tv_color)
            tvSize = itemView.findViewById(R.id.tv_size)
            originPrice = itemView.findViewById(R.id.product_origin_price)
            discountPrice = itemView.findViewById(R.id.product_discount_price)
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent)
        }
    }


    companion object {
        val TAG = "HomeAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeProductViewHolder {
        return HomeProductViewHolder(
            (LayoutInflater.from(parent.context)).inflate(R.layout.item_product_list_home, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeProductViewHolder, position: Int) {
        val product = mListProductData[position].product
        val favorite = mListProductData[position].favorite

        if (holder.ivFavorite == null) {
            Log.d(TAG, "onBindViewHolder: not found icon")
        }

        holder.ivFavorite?.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: $position")
            listener.onClickFavorite(product, favorite)
        }


        holder.productBrand?.text = product.brandName
        holder.productName?.text = product.title

        glideView(holder.productImg!!, holder.layoutLoading!!, product.getImage()!!)

        holder.tvNumberReviews?.text = product.numberReviews.toString()

        holder.tvSize?.text = favorite?.size
        holder.tvColor?.text = favorite?.color
        holder.productRatingBar?.rating = product.reviewStars.toFloat()

        product.salePercent.let {
            if (it != null) {
                holder.tvDiscountPercent?.apply {
                    visibility = View.VISIBLE
                    text = "$it%"
                }
                holder.discountPrice?.visibility = View.VISIBLE
                holder.discountPrice?.text =
                    product.getDiscountPrice().toString() + "$"
                val text = product.getOriginPrice().toString() + "$"
                holder.originPrice?.strike(text)
            } else {
                holder.tvDiscountPercent?.visibility = View.INVISIBLE
                holder.discountPrice?.visibility = View.INVISIBLE
                holder.originPrice?.text = product.getOriginPrice().toString() + "$"
            }
        }

        if (favorite != null) {
            holder.ivFavorite?.setImageResource(R.drawable.ic_favorites_selected)
        }
    }

    override fun getItemCount() = mListProductData.size

    interface IClickListener {
        fun onClickFavorite(product: Product, favorite: Favorite?)
    }

    private fun glideView(imageView: ImageView, loadingLayout: FrameLayout, uri: String) {
        if (uri.contains("https")) {
            Glide
                .with(imageView.context)
                .load(uri)
                .listener(
                    glideListener(loadingLayout)
                )
                .into(imageView)
        } else {
            imageView.setImageURI(Uri.parse(uri))
        }
    }
}



