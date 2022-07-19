package com.goldenowl.ecommerce.ui.global.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemOrderDetailBinding
import com.goldenowl.ecommerce.models.data.CartData
import com.goldenowl.ecommerce.utils.Utils


class OrderDetailAdapter : RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {
    private var mListCartData = listOf<CartData>()

    class ViewHolder(private val binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartData: CartData) {
            val cart = cartData.cart
            val product = cartData.product

            binding.productName.text = product.title

            Utils.glide2View(binding.productImg, binding.layoutLoading.loadingFrameLayout, product.getImage()!!)

            binding.tvSize.text = cart.size
            binding.tvColor.text = cart.color

            binding.cartQuantity.text = cart!!.quantity.toString()

            binding.tvPrice.text = binding.root.context.resources.getString(
                R.string.money_unit_float,
                product.getDiscountPrice()
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOrderDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderData = mListCartData[position]
        holder.bind(orderData)
    }

    override fun getItemCount() = mListCartData.size

    fun setData(listCartData: List<CartData>) {
        mListCartData = listCartData
        notifyDataSetChanged()
    }
}
