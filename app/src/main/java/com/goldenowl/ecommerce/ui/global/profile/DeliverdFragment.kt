package com.goldenowl.ecommerce.ui.global.profile

import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentMyOrderBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.google.android.material.tabs.TabLayoutMediator

class DeliverdFragment : BaseHomeFragment<FragmentMyOrderBinding>() {
    override fun getViewBinding(): FragmentMyOrderBinding {
        return FragmentMyOrderBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        val viewPager = binding.viewPager2
        val tabLayout = binding.topAppBar.tabLayout

        viewPager.adapter = MyOrderAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.delivered)
                1 -> getString(R.string.processing)
                2 -> getString(R.string.cancelled)
                else -> getString(R.string.no_verify)
            }
        }.attach()
    }

    override fun setAppbar() {

    }

    override fun setObservers() {
    }
}