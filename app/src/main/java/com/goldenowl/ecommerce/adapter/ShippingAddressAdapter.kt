package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.databinding.ItemShippingAddressBinding
import com.goldenowl.ecommerce.models.data.Address


class ShippingAddressAdapter(private val listener: ICheckListener) :
    RecyclerView.Adapter<ShippingAddressAdapter.ViewHolder>() {

    private var checkedPosition = 0
    private var listAddress: List<Address> = emptyList()

    interface ICheckListener {
        fun selectAddress(position: Int)
        fun removeAddress(position: Int)
        fun insertAddress(address: Address)
        fun editAddress(position: Int)
    }

    fun setData(listAddress: List<Address>, checkPos: Int) {
        this.listAddress = listAddress
        checkedPosition = checkPos
        notifyDataSetChanged()
    }

    private fun setCheckPos(checkPos: Int) {
        checkedPosition = checkPos
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemShippingAddressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address, position: Int) {
            binding.tvFullName.text = address.fullName
            binding.tvAddress.text = "${address.address}\n${address.city}, ${address.zipCode}, ${address.country}"
            binding.checkDefault.apply {
                isSelected = position == checkedPosition
                setOnClickListener {
                    setCheckPos(position)
                    listener.selectAddress(position)
                }
            }
            binding.tvEdit.setOnClickListener {
                listener.editAddress(position)
            }
            binding.ivRemove.setOnClickListener {
                listener.removeAddress(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShippingAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = listAddress[position]
        holder.bind(address, position)
    }


    override fun getItemCount() = listAddress.size

}
