package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentHomeBinding
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Utils.getColor

class HomeFragment : BaseHomeFragment<FragmentHomeBinding>() {
    val TAG: String = "HomeFragment"

    private var products: List<Product> = ArrayList()
    private lateinit var salesListAdapter: HomeProductListAdapter
    private lateinit var newsListAdapter: HomeProductListAdapter
    private  var favoriteList: List<UserOrder.Favorite> = ArrayList()

    override fun setObservers() {
        viewModel.dataReady.observe(viewLifecycleOwner) {
            if (it == BaseLoadingStatus.LOADING) {
                binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                (requireActivity() as MainActivity).setBottomNavBarEnabled(false)
            } else {
                binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                (requireActivity() as MainActivity).setBottomNavBarEnabled(true)
            }
        }
        viewModel.favoriteList.observe(viewLifecycleOwner){
            favoriteList = it
        }
        viewModel.productsList.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: list changed: $it")
            if (it.isNotEmpty()) {
                products = it
                setAdapters(it)
            }
        }
    }

    private fun setAdapters(listProducts: List<Product>?) {
        if (listProducts != null) {
            Log.d(TAG, "setAdapters: setting adapter")
            salesListAdapter = HomeProductListAdapter(false).apply {
                setData(products, favoriteList)
            }
            binding.rcvSales.adapter = salesListAdapter
            binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            newsListAdapter = HomeProductListAdapter(false).apply {
                setData(products, favoriteList)
            }
            binding.rcvNew.adapter = newsListAdapter
            binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

    }

    override fun init() {
        super.init()

        /* create single userOrder row **/
        viewModel.createUserOrderTable()
    }

    override fun setViews() {
        Log.d(TAG, "setViews: started")
//        salesListAdapter = HomeProductListAdapter(products)
//        binding.rcvSales.adapter = salesListAdapter
//        binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//
//        newsListAdapter = HomeProductListAdapter(products)
//        binding.rcvNew.adapter = newsListAdapter
//        binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


        binding.tvViewAllSale.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("products" to products)
            )
        }

        binding.tvViewAllNew.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("products" to products)
            )
        }
    }



    override fun getViewBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.apply {
            title = getString(R.string.home)
            setCollapsedTitleTextColor(getColor(context, R.color.white) ?: 0xFFFFFF)
            setExpandedTitleColor(getColor(context, R.color.white) ?: 0xFFFFFF)
        }
    }

}
