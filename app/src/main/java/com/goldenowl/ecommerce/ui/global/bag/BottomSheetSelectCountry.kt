package com.goldenowl.ecommerce.ui.global.bag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.goldenowl.ecommerce.adapter.ListCountryAdapter
import com.goldenowl.ecommerce.databinding.ModalBottomSheetSelectCountryBinding
import com.goldenowl.ecommerce.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*

class BottomSheetSelectCountry(private val callback: ISelectCountry) : BottomSheetDialogFragment() {
    private lateinit var binding: ModalBottomSheetSelectCountryBinding

    interface ISelectCountry {
        fun onSelectCountry(country: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = ModalBottomSheetSelectCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }


    private fun setViews() {
        val adapter = ListCountryAdapter(Constants.listCountry, object : ListCountryAdapter.IClickListener {
            override fun onClick(country: String) {
                callback.onSelectCountry(country)
                dismiss()
            }
        })
        binding.rcvCountry.adapter = adapter

        val debounceJob: Job? = null
        val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        var lastInput = ""

        binding.edtSearch.doAfterTextChanged {
            debounceJob?.cancel()
            val newText = it.toString()
            if (lastInput != newText) {
                lastInput = newText
                uiScope.launch {
                    delay(500)
                    if (lastInput == newText)
                        adapter.setData(it.toString())
                }
            }
        }
    }

    companion object {
        const val TAG = "BottomSheetCountry"
    }
}