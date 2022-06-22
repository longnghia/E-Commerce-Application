package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemCountryBinding

class ListCountryAdapter(private val listCountry: List<String>, private val listener: IClickListener) :
    RecyclerView.Adapter<ListCountryAdapter.CategoryViewHolder>() {
    private var fListCountry: List<String> = listCountry

    fun setData(query: String) {
        fListCountry = listCountry.filter {
            it.indexOf(query, ignoreCase = true) >= 0
        }
        notifyDataSetChanged()
    }

    interface IClickListener {
        fun onClick(country: String)
    }

    inner class CategoryViewHolder(val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: String) {
            binding.tvCountry.text = country
            binding.root.setOnClickListener {
                listener.onClick(country)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val country = fListCountry[position]
        holder.bind(country)
    }

    override fun getItemCount(): Int {
        if (fListCountry == null) return 0
        return fListCountry.size
    }
}
