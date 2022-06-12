package com.goldenowl.ecommerce.viewmodels

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.utils.Consts
import com.goldenowl.ecommerce.utils.Consts.SPAN_COUNT_ONE
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.utils.Utils.glideListener
import com.goldenowl.ecommerce.utils.Utils.strike


class FavoriteProductListAdapter(private val mLayoutManager: GridLayoutManager, private val listener: IClickListener) :
    RecyclerView.Adapter<FavoriteProductListAdapter.FavoriteProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()
    private var mListFavoriteProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>, filterType: String?, sortType: SortType?, searchTerm: String) {
        mListProductData = listProductData
        mListFavoriteProductData = getFavoriteProduct()

        if (filterType != null)
            mListFavoriteProductData = mListFavoriteProductData.filter { it.product.categoryName == filterType }

        mListFavoriteProductData = when (sortType) {
            SortType.REVIEW -> mListFavoriteProductData.sortedByDescending { it.product.reviewStars }
            SortType.PRICE_DECREASE -> mListFavoriteProductData.sortedByDescending { it.product.getDiscountPrice() }
            SortType.PRICE_INCREASE -> mListFavoriteProductData.sortedBy { it.product.getDiscountPrice() }
            SortType.POPULAR -> mListFavoriteProductData.sortedByDescending { it.product.isPopular }
            SortType.NEWEST -> mListFavoriteProductData.sortedByDescending { it.product.createdDate }
            else -> mListFavoriteProductData
        }
        if (searchTerm.isNotBlank()) {

            mListFavoriteProductData = mListFavoriteProductData.filter {
                it.product.title.indexOf(searchTerm, ignoreCase = true) >= 0 || it.product.brandName.indexOf(
                    searchTerm,
                    ignoreCase = true
                ) >= 0
            }
        }
        Log.d(TAG, "setData: mListFavoriteProductData = $mListFavoriteProductData")

        notifyDataSetChanged()
    }

    private fun getFavoriteProduct(): List<ProductData> {
        return mListProductData.filter {
            it.favorite != null
        }
    }

    class FavoriteProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var productName: TextView? = null
        var productBrand: TextView? = null
        var tvColor: TextView? = null
        var tvDiscoutPercent: TextView? = null
        var tvSize: TextView? = null
        var originPrice: TextView? = null
        var discountPrice: TextView? = null
        var tvNumberReviews: TextView? = null
        var productImg: ImageView? = null
        var productRatingBar: RatingBar? = null
        var ivCart: ImageView? = null
        var ivRemove: ImageView? = null
        var layoutLoading: FrameLayout? = null
        var layoutFrameLoading: FrameLayout? = null

        init {
            productName = itemView.findViewById(R.id.product_name)
            productBrand = itemView.findViewById(R.id.product_brand)
            productImg = itemView.findViewById(R.id.product_img)
            productRatingBar = itemView.findViewById(R.id.product_rating_bar)
            tvNumberReviews = itemView.findViewById(R.id.tv_number_reviews)
            ivCart = itemView.findViewById(R.id.iv_cart)
            ivRemove = itemView.findViewById(R.id.iv_remove)
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
            tvDiscoutPercent = itemView.findViewById(R.id.tv_discount_percent)
        }
    }


    companion object {
        val TAG = "FavoriteAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductViewHolder {
        if (viewType == Consts.GRID_VIEW) {
            return FavoriteProductViewHolder(
                (LayoutInflater.from(parent.context)).inflate(
                    R.layout.item_product_list_favorite_grid,
                    parent,
                    false
                )
            )
        }
        return FavoriteProductViewHolder(
            (LayoutInflater.from(parent.context)).inflate(R.layout.item_product_list_favorite, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavoriteProductViewHolder, position: Int) {
        val product = mListFavoriteProductData[position].product
        val cart = mListFavoriteProductData[position].cart
        val favorite = mListFavoriteProductData[position].favorite

        if (holder.ivCart == null) {
            Log.d(TAG, "onBindViewHolder: not found icon")
        }

        holder.ivCart?.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: $position")
            listener.onClickCart(product, cart)
        }

        holder.ivRemove?.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: $position")
            listener.onClickRemove(product, favorite)
        }

        holder.productBrand?.text = product.brandName
        holder.productName?.text = product.title

        glideView(holder.productImg!!, holder.layoutLoading!!, product.getImage()!!)

        holder.tvNumberReviews?.text = product.numberReviews.toString()
        holder.tvSize?.text = favorite?.size
        holder.tvColor?.text = favorite?.color

        product.salePercent.let {
            if (it != null) {
                holder.tvDiscoutPercent?.apply {
                    visibility = View.VISIBLE
                    text = "$it%"
                }
                holder.discountPrice?.visibility = View.VISIBLE
                holder.discountPrice?.text =
                    product.getDiscountPrice().toString() + "$"
                val text = product.getOriginPrice().toString() + "$"
                holder.originPrice?.strike(text)
            } else {
                holder.tvDiscoutPercent?.visibility = View.INVISIBLE
                holder.discountPrice?.visibility = View.INVISIBLE
                holder.originPrice?.text = product.getOriginPrice().toString() + "$"
            }
        }
        holder.productRatingBar?.rating = product.reviewStars.toFloat()
        if (cart != null) {
            holder.ivCart?.setImageResource(R.drawable.ic_bag_selected)
            Log.d(TAG, "onBindViewHolder: cart: $cart")
        }else{
            holder.ivCart?.setImageResource(R.drawable.ic_cart)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val spanCount = mLayoutManager.spanCount
        return if (spanCount == SPAN_COUNT_ONE) {
            Consts.LIST_VIEW
        } else {
            Consts.GRID_VIEW
        }
    }

    override fun getItemCount() = mListFavoriteProductData.size

    interface IClickListener {
        fun onClickCart(product: Product, cart: Cart?)
        fun onClickRemove(product: Product, favorite: Favorite?)
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

