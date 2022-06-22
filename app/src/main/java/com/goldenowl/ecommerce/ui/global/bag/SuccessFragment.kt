package com.goldenowl.ecommerce.ui.global.bag

import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSuccessBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment

class SuccessFragment : BaseHomeFragment<FragmentSuccessBinding>() {
    override fun getViewBinding(): FragmentSuccessBinding {
        return FragmentSuccessBinding.inflate(layoutInflater)
    }


    companion object {
        const val TAG = "SuccessFragment"
    }

    override fun setViews() {
        binding.btnContinueShopping.setOnClickListener {
            findNavController().navigate(R.id.home_dest)
        }
    }

    override fun setObservers() {
    }

    override fun setAppbar() {
    }
}
