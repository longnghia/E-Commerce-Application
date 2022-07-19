package com.goldenowl.ecommerce.ui.global.profile

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentProcessingBinding
import com.goldenowl.ecommerce.models.data.Order
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants

class ProcessingFragment(private val status: Order.Companion.OrderStatus) :
    BaseHomeFragment<FragmentProcessingBinding>(), OrderAdapter.OrderClickListener {
    private lateinit var adapter: OrderAdapter
    private var listOrder: List<Order> = emptyList()

    override fun getViewBinding(): FragmentProcessingBinding {
        return FragmentProcessingBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        adapter = OrderAdapter(this)
        binding.rcvOrder.adapter = adapter
    }

    override fun setAppbar() {

    }

    override fun setObservers() {
        viewModel.allOrder.observe(viewLifecycleOwner) {
            listOrder = it.reversed().filter { order ->
                order.status == status
            }
            if (listOrder.isEmpty()) {
                binding.tvNoOrders.visibility = View.VISIBLE
                binding.rcvOrder.visibility = View.INVISIBLE
            } else {
                binding.rcvOrder.visibility = View.VISIBLE
                binding.tvNoOrders.visibility = View.INVISIBLE
                adapter.setData(listOrder)
            }
        }
    }

    override fun onClickDetail(order: Order) {
        findNavController().navigate(R.id.action_go_detail, bundleOf(Constants.KEY_ORDER to order))
    }
}