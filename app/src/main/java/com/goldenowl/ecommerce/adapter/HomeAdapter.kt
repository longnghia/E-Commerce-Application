package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemHomeCategoryBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.IClickListener
import com.goldenowl.ecommerce.utils.Constants


class HomeAdapter(
    private val listener: IClickListener,
    private val listCategory: List<String>,
    private val navigate: (String) -> Unit
) :
    RecyclerView.Adapter<HomeAdapter.HomeProductViewHolder>() {

    private var mListProductData = listOf<ProductData>()

    fun setData(listProductData: List<ProductData>) {
        mListProductData = listProductData
        notifyDataSetChanged()
    }

    inner class HomeProductViewHolder(private val binding: ItemHomeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            val title: String
            when (category) {
                Constants.KEY_SALE -> {
                    title = binding.root.context.resources.getString(R.string.sales)
                    binding.tvSubtitle.text = binding.root.context.resources.getString(R.string.summer_sale)
                }
                Constants.KEY_NEW -> {
                    title = binding.root.context.resources.getString(R.string.news)
                    binding.tvSubtitle.text = binding.root.context.resources.getString(R.string.never_seen_before)
                }
                else -> {
                    title = category
                    binding.tvSubtitle.visibility = View.GONE
                }
            }
            binding.tvCategory.text = title
            val adapter = HomeProductListAdapter(listener)
            binding.rcvCategory.adapter = adapter
            adapter.setData(mListProductData, category)

            binding.tvViewAllNew.setOnClickListener {
                navigate(category)
            }
        }
    }


    companion object {
        val TAG = "HomeAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeProductViewHolder {
        return HomeProductViewHolder(
            ItemHomeCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeProductViewHolder, position: Int) {
        val category = listCategory[position]
        holder.bind(category)
    }

    override fun getItemCount() = listCategory.size
}



