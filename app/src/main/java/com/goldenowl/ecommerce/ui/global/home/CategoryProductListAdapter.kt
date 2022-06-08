package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemProductListCategoryBinding
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder
import kotlinx.android.synthetic.main.item_product_list_home.view.*

class CategoryProductListAdapter(private val listenner: IClickListener) :
    RecyclerView.Adapter<CategoryProductListAdapter.ProductViewHolder>() {

    interface IClickListener {
        fun onClickFavorite(product: Product)
    }

    private var productList = listOf<Product>()
    private var favoriteList = listOf<String>()

    fun setData(product: List<Product>, favoriteList: List<UserOrder.Favorite>) {
        Log.d("CategoryListAdapter", "setData: set new data")
        this@CategoryProductListAdapter.productList = product
        this@CategoryProductListAdapter.favoriteList = favoriteList.map { it.productId }
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: ItemProductListCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, listener: IClickListener) {
            binding.productName.text = product.title
            binding.productBrand.text = product.brandName
            product.getImage().let {
                if (it != null) {
                    Glide
                        .with(binding.productImg.context)
                        .load(it)
                        .listener(HomeProductListAdapter.glideListener(binding.layoutLoading.loadingFrameLayout))
                        .into(binding.productImg)
                }
            }
            binding.layoutRating.product_rating_bar.rating = product.reviewStars.toFloat()
            product.salePercent.let {
                if (it != null) {
                    binding.productCurrentPrice.text = product.getDiscountPrice().toString() + "$"
                } else {
                    binding.productCurrentPrice.text = product.getOriginPrice().toString() + "$"
                }
            }
            binding.tvRateCount.text = product.numberReviews.toString()
            binding.ivFavorite.setOnClickListener {
                listener.onClickFavorite(product)
            }
            if (favoriteList.indexOf(product.id) >= 0) {
                binding.ivFavorite.setBackgroundResource(R.drawable.ic_favorites_selected)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemBinding =
            ItemProductListCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product, listenner)
    }

    override fun getItemCount() = productList.size
}


