package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemProductListBagBinding
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.IClickListener
import com.goldenowl.ecommerce.utils.Utils


class BagProductListAdapter(private val listener: IClickListener) :
    RecyclerView.Adapter<BagProductListAdapter.BagProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()
    private var mListCartProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>, searchTerm: String) {
        mListProductData = listProductData
        mListCartProductData = getCartProduct()
        if (searchTerm.isNotBlank()) {
            mListCartProductData = mListCartProductData.filter {
                it.product.title.indexOf(searchTerm, ignoreCase = true) >= 0 || it.product.brandName.indexOf(
                    searchTerm,
                    ignoreCase = true
                ) >= 0
            }
        }

        notifyDataSetChanged()
    }

    private fun getCartProduct(): List<ProductData> {
        return mListProductData.filter {
            it.cart != null
        }
    }

    inner class BagProductViewHolder(private val binding: ItemProductListBagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(productData: ProductData, position: Int) {
            val product = productData.product
            val cart = productData.cart
            val favorite = productData.favorite

            binding.ivMore?.setOnClickListener {
                showMenu(binding.tvAnchor as View, product, favorite, cart)
            }

            binding.root?.setOnClickListener {
                listener.onClickItem(productData)
            }

            binding.productName?.text = product.title

            Utils.glide2View(binding.productImg!!, binding.layoutLoading.loadingFrameLayout, product.getImage()!!)

            binding.tvSize?.text = cart?.size
            binding.tvColor?.text = cart?.color

            binding.tvQuantity?.text = cart!!.quantity.toString()

            product.getPriceByCart(cart).also {
                if (it != null) {
                    val price = it * cart.quantity
                    binding.tvPrice?.text = binding.root.context.resources.getString(R.string.money_unit_float, price)
                }
            }


            binding.tvAdd?.setOnClickListener {
                cart?.quantity++
                listener.updateCartQuantity(cart, position)
                notifyItemChanged(position)
            }

            binding.tvSubtract?.setOnClickListener {
                if (cart.quantity == 1) {
                    listener.onClickCart(product, cart)
                } else {
                    cart!!.quantity--
                    listener.updateCartQuantity(cart, position)
                }
                notifyItemChanged(position)
            }
        }
    }

    fun getPrice(promo: Int): Float {
        var total = 0f
        for (productData in mListCartProductData) {
            val product = productData.product
            val cart = productData.cart
            if (cart == null) {
                return -1f
            } else
                total += (product.getPriceByCart(cart) ?: 0f) * cart.quantity
        }
        return if (promo == 0) total
        else total * (100 - promo) / 100f
    }

    fun getBag(): List<ProductData> {
        return mListCartProductData
    }

    companion object {
        const val TAG = "BagAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BagProductViewHolder {
        return BagProductViewHolder(
            ItemProductListBagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BagProductViewHolder, position: Int) {
        val productData = mListCartProductData[position]
        holder.bind(productData, position)
    }

    private fun showMenu(anchor: View, product: Product, favorite: Favorite?, cart: Cart?) {
        val listPopupWindow = ListPopupWindow(anchor.context, null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = anchor

        var items: List<String> = if (favorite != null)
            listOf("Remove from favorite", "Delete from the list")
        else {
            listOf("Add to favorite", "Delete from the list")
        }
        val adapter = ArrayAdapter(anchor.context, R.layout.list_popup_window_item, items)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            when (position) {
                0 -> {
                    listener.onClickFavorite(product, favorite)
                }
                1 -> {
                    listener.onClickCart(product, cart)
                }
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

    override fun getItemCount() = mListCartProductData.size

}

