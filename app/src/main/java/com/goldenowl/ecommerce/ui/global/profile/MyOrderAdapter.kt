package com.goldenowl.ecommerce.ui.global.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goldenowl.ecommerce.models.data.Order


class MyOrderAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProcessingFragment(Order.Companion.OrderStatus.DELIVERED)
            1 -> ProcessingFragment(Order.Companion.OrderStatus.PROCESSING)
            2 -> ProcessingFragment(Order.Companion.OrderStatus.CANCELLED)
            else -> ProcessingFragment(Order.Companion.OrderStatus.PROCESSING)

        }
    }
}
