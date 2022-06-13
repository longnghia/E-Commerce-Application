package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ModalBottomSheetAddToFavoriteBinding
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetInsertFavorite(private val product: Product, private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {
//    private val viewModel: ProductViewModel by activityViewModels {
//        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
//    }

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

//        val product = arguments?.get("product") as Product
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
            viewModel.insertFavorite(
                Favorite(
                    product.id,
                    mapString[binding.radioGroup.checkedRadioButtonId]!!,
                    product.getFirstColor()
                )
            )
            dismiss()
        }

    }

    private fun setRadioButton(v: RadioButton, available: Boolean) {
        v.isEnabled = true // todo check if product quantity available
    }

    companion object {
        const val TAG = "BottomSheetInsertFavorite"
    }
}