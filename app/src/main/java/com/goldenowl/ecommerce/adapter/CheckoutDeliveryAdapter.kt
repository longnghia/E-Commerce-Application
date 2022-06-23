package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemDeliveryBinding
import com.goldenowl.ecommerce.models.data.Delivery
import com.goldenowl.ecommerce.utils.Constants

class CheckoutDeliveryAdapter(private val listener: IClickDelivery) :
    RecyclerView.Adapter<CheckoutDeliveryAdapter.ViewHolder>() {
    private val listDelivery = Constants.listDelivery
    private var selected = -1

    interface IClickDelivery {
        fun onClickDelivery(delivery: Delivery)
    }

    fun setSelected(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemDeliveryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(delivery: Delivery, position: Int) {
            binding.ivDeliveryImg.setImageResource(delivery.logo)
            binding.tvDeliveryTime.text = delivery.time
            binding.root.setOnClickListener {
                listener.onClickDelivery(delivery)
                setSelected(position)
            }
            if (position == selected) {
                binding.cardViewDelivery.strokeWidth = 3
            } else
                binding.cardViewDelivery.strokeWidth = 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeliveryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val delivery = listDelivery[position]
        holder.bind(delivery, position)
    }

    override fun getItemCount() = listDelivery.size
}

