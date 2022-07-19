package com.goldenowl.ecommerce.ui.global.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemOrderBinding
import com.goldenowl.ecommerce.models.data.Order
import com.goldenowl.ecommerce.utils.SimpleDateFormatHelper


class OrderAdapter(private val listener: OrderClickListener) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private var mListOrder: List<Order> = listOf()

    interface OrderClickListener {
        fun onClickDetail(order: Order)
    }

    fun setData(listOrder: List<Order>) {
        mListOrder = listOrder
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun getString(resId: Int): String {
            return binding.root.resources.getString(resId)
        }

        fun bind(order: Order) {
            binding.orderId.text = order.orderId
            binding.orderDate.text = SimpleDateFormatHelper.formatDate(order.date)
            binding.orderTrackingNumber.text = order.trackingNumber
            binding.orderTotal.text =
                binding.root.context.resources.getString(R.string.money_unit_float, order.totalAmount)
            binding.orderQuantity.text = order.listCart.size.toString()
            binding.orderStatus.text = when (order.status) {
                Order.Companion.OrderStatus.PROCESSING -> getString(R.string.processing)
                Order.Companion.OrderStatus.DELIVERED -> getString(R.string.delivered)
                Order.Companion.OrderStatus.CANCELLED -> getString(R.string.cancelled)
            }
            binding.btnDetail.setOnClickListener {
                listener.onClickDetail(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = mListOrder[position]
        holder.bind(order)
    }

    override fun getItemCount() = mListOrder.size
}
