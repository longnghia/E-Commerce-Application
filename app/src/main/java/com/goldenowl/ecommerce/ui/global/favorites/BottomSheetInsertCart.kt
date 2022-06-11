package com.goldenowl.ecommerce.ui.global.favorites

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ModalBottomSheetAddToFavoriteBinding
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetInsertCart(private val product: Product, private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetAddToFavoriteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetAddToFavoriteBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddToFavorites.text = getString(R.string.add_to_cart)
        val mapString = mapOf(
            R.id.radio_size_L to "L",
            R.id.radio_size_M to "M",
            R.id.radio_size_S to "S",
            R.id.radio_size_XL to "XL",
            R.id.radio_size_XS to "XS"
        )
        val mapView = mapOf(
            "L" to binding.radioSizeL,
            "M" to binding.radioSizeM,
            "S" to binding.radioSizeS,
            "XL" to binding.radioSizeXL,
            "XS" to binding.radioSizeXS
        )

        if (product != null) {
            val listSize = product.getListSize()
            if (listSize.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: list size = $listSize")
                for (size in listSize) {
                    val s = size.size
                    val view = mapView[s] as RadioButton

                    val available = size.quantity > 0
                    setRadioButton(view, available)
                }
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.d(TAG, "onViewCreated: checked id = $checkedId, size=${mapString[checkedId]}")
//            .setTextColor(requireContext().getColor(R.color.white))
        }

        binding.btnAddToFavorites.setOnClickListener {
            viewModel.insertCart(
                Cart(
                    product.id,
                    mapString[binding.radioGroup.checkedRadioButtonId]!!,
                    "black",
                    1
                )
            )
            dismiss()
        }

    }

    private fun setRadioButton(v: RadioButton, available: Boolean) {
        v.isEnabled = available
    }

    companion object {
        const val TAG = "BottomSheetAddToCart"
    }
}