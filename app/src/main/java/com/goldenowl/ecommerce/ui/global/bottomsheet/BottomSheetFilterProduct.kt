package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ModalBottomSheetFilterProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider

class BottomSheetFilterProduct(private val values: List<Float>?, private val listener: OnFilterListener) :
    BottomSheetDialogFragment() {
    private lateinit var binding: ModalBottomSheetFilterProductBinding
    lateinit var slider: RangeSlider
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetFilterProductBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        slider = binding.rangeSlider
        slider.values = values ?: listOf(10f, 70f)

        slider.apply {
            binding.tvValueFrom.text = getString(R.string.money_unit_float, values[0])
            binding.tvValueTo.text = getString(R.string.money_unit_float, values[1])

            setLabelFormatter { value: Float ->
                getString(R.string.money_unit_float, value)
            }
            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {
                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                }
            })

            addOnChangeListener { slider, value, fromUser ->
                binding.tvValueFrom.text = getString(R.string.money_unit_float, slider.values[0])
                binding.tvValueTo.text = getString(R.string.money_unit_float, slider.values[1])
            }
        }

        binding.btnDiscard.setOnClickListener {
            listener.discard()
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            listener.apply(slider.values)
            dismiss()
        }
    }

    interface OnFilterListener {
        fun discard()
        fun apply(values: List<Float>)
    }

    companion object {
        val TAG = "BottomSheetFilterProduct"
    }
}