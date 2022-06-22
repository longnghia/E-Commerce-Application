package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.adapter.BottomSheetPromoAdapter
import com.goldenowl.ecommerce.databinding.ModalBottomSheetEnterPromoBinding
import com.goldenowl.ecommerce.models.data.Promo
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetEnterPromo(private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetEnterPromoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetEnterPromoBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetAdapter = BottomSheetPromoAdapter(
            viewModel.listPromo.value ?: emptyList(),
            object : BottomSheetPromoAdapter.IClickListenerPromo {
                override fun onClick(promo: Promo) {
                    val bag = viewModel.curBag.value
                    bag?.promo = promo
                    viewModel.curBag.value = bag
                    dismiss()
                }
            })

        binding.rcvPromo.adapter = bottomSheetAdapter
        binding.rcvPromo.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        const val TAG = "BottomSheetAddToCart"
    }
}