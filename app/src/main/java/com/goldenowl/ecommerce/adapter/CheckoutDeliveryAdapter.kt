package com.goldenowl.ecommerce.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemDeliveryBinding
import com.goldenowl.ecommerce.models.data.Delivery
import com.goldenowl.ecommerce.utils.Consts

class CheckoutDeliveryAdapter : RecyclerView.Adapter<CheckoutDeliveryAdapter.ViewHolder>() {
    private val listDelivery = Consts.listDelivery

    class ViewHolder(private val binding: ItemDeliveryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(delivery: Delivery) {
            binding.ivDeliveryImg.setImageResource(delivery.logo)
            binding.tvDeliveryTime.text = delivery.time
            binding.root.setOnClickListener {
                Log.d("CheckoutDeliveryAdapter", "bind: clicked $delivery")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeliveryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val delivery = listDelivery[position]
        holder.bind(delivery)
    }

    override fun getItemCount() = listDelivery.size
}

