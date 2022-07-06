package com.goldenowl.ecommerce.ui.global.profile

import android.app.SearchManager
import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentOrderDetailBinding
import com.goldenowl.ecommerce.models.data.CartData
import com.goldenowl.ecommerce.models.data.Order
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class OrderDetailFragment : BaseHomeFragment<FragmentOrderDetailBinding>() {
    private lateinit var mOrder: Order
    private lateinit var listCartData: List<CartData>

    override fun getViewBinding(): FragmentOrderDetailBinding {
        return FragmentOrderDetailBinding.inflate(layoutInflater)
    }

    private val sortViewModel = SortFilterViewModel()

    private var searchTerm: String = ""

    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null


    override fun setObservers() {
        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            when (it) {
                BaseLoadingStatus.LOADING -> binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                BaseLoadingStatus.SUCCEEDED -> {
                    binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.bag_dest)
                }
                else -> binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
            }
        }
    }

    override fun setViews() {
        binding.orderId.text = mOrder.orderId
        binding.orderDate.text = SimpleDateFormat("MMMM dd, YYYY").format(mOrder.date)
        binding.orderTrackingNumber.text = mOrder.trackingNumber
        binding.orderTotalAmount.text =
            getString(R.string.money_unit_float, mOrder.totalAmount)
        binding.orderQuantity.text =
            context?.resources?.getQuantityString(R.plurals.num_item, mOrder.listCart.size, mOrder.listCart.size)

        binding.orderStatus.text = when (mOrder.status) {
            Order.Companion.OrderStatus.PROCESSING -> getString(R.string.processing)
            Order.Companion.OrderStatus.DELIVERED -> getString(R.string.delivered)
            Order.Companion.OrderStatus.CANCELLED -> getString(R.string.cancelled)
        }
        val adapter = OrderDetailAdapter()
        binding.rcvOrder.adapter = adapter
        listCartData = viewModel.getListCartData(mOrder)
        adapter.setData(listCartData)

        binding.orderShippingAddress.text = mOrder.shippingAddress
        binding.orderPaymentMethod.text = mOrder.cardId
        binding.orderDiscount.text = mOrder.promoCode

        binding.btnReorder.setOnClickListener {
            viewModel.restoreCart(mOrder)
        }

        if (mOrder.promoCode.isNotEmpty()) {
            val promo = viewModel.listPromo.value?.find {
                it.id == mOrder.promoCode
            }
            if (promo != null)
                binding.orderDiscount.text = "${promo.salePercent}%, ${promo.name}"
        } else {
            binding.orderDiscount.text = getString(R.string.no_discount)
        }

        val cardImg = when ((mOrder.cardId)[0]) {
            '4' -> (R.drawable.ic_master_card_2)
            '5' -> (R.drawable.ic_visa)
            else -> (R.drawable.ic_master_card_2)
        }
        binding.ivCardImg.setImageResource(cardImg)
        val delivery = Constants.listDelivery.find { it.id == mOrder.delivery }
        if (delivery != null)
            binding.orderDeliveryMethod.text = "${delivery.id}, ${delivery.time}, ${delivery.price}"
    }

    override fun init() {
        super.init()
        mOrder = arguments?.get(Constants.KEY_ORDER) as Order
    }

    private fun setAppBarMenu() {
        binding.topAppBar.toolbar.apply {
            if (menu.isEmpty()) {
                inflateMenu(R.menu.menu_search)
                val searchItem = menu.findItem(R.id.ic_search)
                val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

                if (searchItem != null) {
                    searchView = searchItem.actionView as SearchView
                }
                if (searchView != null) {
                    val debounceJob: Job? = null
                    val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
                    var lastInput = ""

                    searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
                    queryTextListener = object : SearchView.OnQueryTextListener {
                        override fun onQueryTextChange(newText: String?): Boolean {
                            debounceJob?.cancel()
                            if (lastInput != newText) {
                                lastInput = newText ?: ""
                                uiScope.launch {
                                    delay(500)
                                    if (lastInput == newText) {
                                        sortViewModel.searchTerm.value = newText
                                    }
                                }
                            }
                            return true
                        }

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            Log.d("onQueryTextSubmit", query!!)
                            hideKeyboard()
                            return true
                        }
                    }
                    searchView!!.setOnCloseListener {
                        false
                    }
                    searchView!!.maxWidth = Integer.MAX_VALUE
                    searchView!!.setOnQueryTextListener(queryTextListener)
                }
            }
        }
    }


    companion object {
        const val TAG = "OrderDetail"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.order_details)

        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setAppBarMenu()
    }
}
