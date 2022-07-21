package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ItemBottomSheetPromoBinding
import com.goldenowl.ecommerce.models.data.Promo
import com.goldenowl.ecommerce.utils.Utils

class BottomSheetPromoAdapter(private val listPromo: List<Promo>, private val listener: IClickListenerPromo) :
    RecyclerView.Adapter<BottomSheetPromoAdapter.ViewHolder>() {

    interface IClickListenerPromo {
        fun onClick(promo: Promo)
    }

    inner class ViewHolder(private val binding: ItemBottomSheetPromoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(promo: Promo) {

            binding.tvPromoTitle.text = promo.name
            binding.tvPromoCode.text = promo.id
            binding.tvDiscountPercent.text = promo.salePercent.toString()
            Utils.glide2View(binding.ivPromo, binding.layoutLoading.loadingFrameLayout, promo.backgroundImage)

            promo.getDayRemain().let {
                if (it == 0) {
                    binding.layoutGreyOut.root.visibility = View.VISIBLE
                    binding.tvDayRemain.text =
                        binding.root.context.resources.getString(R.string.expired)
                    binding.btnApply.setOnClickListener { v ->
                        binding.root.context.let { context ->
                            Toast.makeText(context, context.getString(R.string.promo_expired), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    binding.layoutGreyOut.root.visibility = View.INVISIBLE
                    binding.tvDayRemain.text =
                        binding.root.context.resources.getQuantityString(R.plurals.num_day_left, it, it)
                    binding.btnApply.setOnClickListener { v ->
                        listener.onClick(promo)
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBottomSheetPromoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val promo = listPromo[position]
        holder.bind(promo)
    }

    override fun getItemCount() = listPromo.size

    companion object {
        val TAG = "BottomSheetPromoAdapter"
    }
}

