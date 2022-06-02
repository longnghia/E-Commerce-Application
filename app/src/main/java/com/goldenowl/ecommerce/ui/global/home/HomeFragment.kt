package com.goldenowl.ecommerce.ui.global.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Utils.getColor
import com.goldenowl.ecommerce.viewmodels.ProductViewModel
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory

class HomeFragment : Fragment() {
    private lateinit var binding: com.goldenowl.ecommerce.databinding.FragmentHomeBinding
    val TAG: String = "HomeFragment"
    private var products: List<Product> = ArrayList()
    private lateinit var salesListAdapter: HomeProductListAdapter
    private lateinit var newsListAdapter: HomeProductListAdapter

    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: " + TAG)
        binding = com.goldenowl.ecommerce.databinding.FragmentHomeBinding.inflate(layoutInflater, container, false)
        setViews()
        setObservers()
        return binding.root
    }

    private fun setObservers() {
        viewModel.dataReady.observe(viewLifecycleOwner) {
            if (it == BaseLoadingStatus.LOADING) {
                binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
            } else {
                binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
            }
        }
        viewModel.productsList.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: list changed: $it")
            if (it.isNotEmpty()) {
                products = it
                setAdapters(it)
            }
        }

        viewModel.testText.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: $it")
        }
    }

    private fun setAdapters(listProducts: List<Product>?) {
        if (listProducts != null) {
            Log.d(TAG, "setAdapters: setting adapter")
            salesListAdapter = HomeProductListAdapter(false).apply {
                setData(products)
            }
            binding.rcvSales.adapter = salesListAdapter
            binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            newsListAdapter = HomeProductListAdapter(false).apply {
                setData(products)
            }
            binding.rcvNew.adapter = newsListAdapter
            binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun setViews() {
        Log.d(TAG, "setViews: started")
        setAppBar()
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
            viewModel.testText.value = "Hello from 123"
        }

        binding.tvViewAllNew.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("products" to products)
            )
        }
    }


    private fun setAppBar() {
        binding.topAppBar.collapsingToolbarLayout.apply {
            title = getString(R.string.home)
            setCollapsedTitleTextColor(getColor(context, R.color.white) ?: 0xFFFFFF)
            setExpandedTitleColor(getColor(context, R.color.white) ?: 0xFFFFFF)
        }
    }

}
