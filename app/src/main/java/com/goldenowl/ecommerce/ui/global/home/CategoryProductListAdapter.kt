package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.databinding.ItemProductListCategoryBinding
import com.goldenowl.ecommerce.models.data.Product
import kotlinx.android.synthetic.main.item_product_list_home.view.*

class CategoryProductListAdapter() :
    RecyclerView.Adapter<CategoryProductListAdapter.ProductViewHolder>() {

    private var productList = listOf<Product>()

    fun setData(product: List<Product>){
        Log.d("CategoryListAdapter", "setData: set new data")
        productList = product
        notifyDataSetChanged()
    }
    class ProductViewHolder(private val binding: ItemProductListCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
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

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemBinding =
            ItemProductListCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount() = productList.size
}


