package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ModalBottomSheetSortProductBinding
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetSortProduct(private val viewModel: SortFilterViewModel) : BottomSheetDialogFragment() {
    private lateinit var binding: ModalBottomSheetSortProductBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetSortProductBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = mapOf<SortType, TextView>(
            SortType.POPULAR to binding.sortByPopular,
            SortType.NEWEST to binding.sortByNewest,
            SortType.PRICE_INCREASE to binding.sortByPriceInsc,
            SortType.PRICE_DECREASE to binding.sortByPriceDesc,
            SortType.REVIEW to binding.sortByReview
        )

        for (pair in map) {
            val (type, view) = pair
            view.setOnClickListener {
                viewModel.setSortType(type)
                dismiss()
            }
        }

        viewModel.sortType.observe(viewLifecycleOwner) {
            for (pair in map) {
                val (type, view) = pair
                if (type == it) {
                    setViewBackground(view, R.color.red_dark)
                } else {
                    view.setBackgroundColor(0)
                }

            }
        }
    }

    private fun setViewBackground(v: TextView, c: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v.setBackgroundColor(resources.getColor(c, activity?.theme))
            v.setTextColor(requireContext().getColor(R.color.white))
        } else {
            v.setBackgroundColor(resources.getColor(c))
        }
    }

    companion object {
        const val TAG = "BottomSheetSortProduct"
    }
}