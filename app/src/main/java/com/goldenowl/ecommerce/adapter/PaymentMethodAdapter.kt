package com.goldenowl.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Card
import com.goldenowl.ecommerce.utils.Constants


class PaymentMethodAdapter(private val listener: ICheckListener) :
    RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder>() {

    private var checkedPosition = 0
    private var listCard: List<Card> = emptyList()

    interface ICheckListener {
        fun selectCard(position: Int)
        fun removeCard(position: Int)
        fun insertCard(card: Card)
    }

    fun setData(listCard: List<Card>, checkPos: Int) {
        this.listCard = listCard
        checkedPosition = checkPos
        notifyDataSetChanged()
    }

    private fun setCheckPos(checkPos: Int) {
        checkedPosition = checkPos
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvHolderName: TextView? = null
        var tvExpireDay: TextView? = null
        var tvCardNumber: TextView? = null
        var checkDefault: ImageView? = null
        var ivRemove: ImageView? = null

        init {
            tvHolderName = view.findViewById(R.id.tv_holder_name)
            tvExpireDay = view.findViewById(R.id.tv_expire_day)
            tvCardNumber = view.findViewById(R.id.tv_card_number)
            checkDefault = view.findViewById(R.id.check_default)
            ivRemove = view.findViewById(R.id.ic_remove)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            Constants.TYPE_MATER_CARD -> {
                ViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_payment_master_card, parent, false
                    )
                )
            }
            else -> ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_payment_visa, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = listCard[position]
        holder.tvHolderName?.text = card.cardName
        holder.tvCardNumber?.text = card.cardNumber
        holder.tvExpireDay?.text = card.expireDate

        holder.checkDefault?.isSelected = position == checkedPosition
        holder.checkDefault?.setOnClickListener {
            setCheckPos(position)
            listener.selectCard(position)
        }
        holder.ivRemove?.setOnClickListener {
            listener.removeCard(position)
        }
    }

    override fun getItemCount() = listCard.size

    override fun getItemViewType(position: Int): Int {
        return when (listCard[position].cardNumber[0]) {
            '4' -> Constants.TYPE_MATER_CARD
            '5' -> Constants.TYPE_VISA
            else -> Constants.TYPE_VISA
        }
    }

}
