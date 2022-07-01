package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            binding.btnApply.setOnClickListener {
                listener.onClick(promo)
            }
            binding.tvPromoTitle.text = promo.name
            binding.tvPromoCode.text = promo.id
            binding.tvDiscountPercent.text = promo.salePercent.toString()
            Utils.glide2View(binding.ivPromo, binding.layoutLoading.loadingFrameLayout, promo.backgroundImage)

            binding.tvDayRemain.text =
                promo.getDayRemain().let {
                    if (it > 1) "$it days left" else "$it day left"
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

