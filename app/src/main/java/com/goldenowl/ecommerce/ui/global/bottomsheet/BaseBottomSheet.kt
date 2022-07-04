package com.goldenowl.ecommerce.ui.global.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet<VBinding : ViewBinding>() :
    BottomSheetDialogFragment() {

    lateinit var binding: VBinding
    protected val viewModel: ShopViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    abstract fun getViewBinding(): VBinding

    open fun initData() {
        viewModel.loadingStatus.value = BaseLoadingStatus.NONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = getViewBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObservers()
    }

    abstract fun setObservers()

    abstract fun setViews()

    companion object {
        const val TAG = "BottomSheet"
    }
}