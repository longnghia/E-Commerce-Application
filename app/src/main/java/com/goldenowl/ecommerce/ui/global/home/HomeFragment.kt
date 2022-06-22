package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeProductListAdapter
import com.goldenowl.ecommerce.databinding.FragmentHomeBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Utils.autoScroll
import com.goldenowl.ecommerce.utils.Utils.getColor

class HomeFragment : BaseHomeFragment<FragmentHomeBinding>() {
    val TAG: String = "HomeFragment"

    private lateinit var salesListAdapter: HomeProductListAdapter
    private lateinit var newsListAdapter: HomeProductListAdapter

    private var listProductData: List<ProductData> = emptyList()

    override fun setObservers() {
        viewModel.dataReady.observe(viewLifecycleOwner) {
            if (it == BaseLoadingStatus.LOADING) {
                binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                (requireActivity() as MainActivity).setBottomNavBarEnabled(false)
            } else {
                binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                (requireActivity() as MainActivity).setBottomNavBarEnabled(true)
            }

            viewModel.listProductData.observe(viewLifecycleOwner) {
                listProductData = it
                salesListAdapter.setData(listProductData, "Sales")
                newsListAdapter.setData(listProductData, "News")
            }

            viewModel.allFavorite.observe(viewLifecycleOwner) {
                viewModel.reloadListProductData()
            }
            viewModel.toastMessage.observe(viewLifecycleOwner) {
                if (!it.isNullOrBlank())
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun setViews() {
        salesListAdapter = HomeProductListAdapter(this)
        newsListAdapter = HomeProductListAdapter(this)
        binding.rcvSales.adapter = salesListAdapter
        binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvNew.adapter = newsListAdapter
        binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvViewAllSale.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("home_filter" to "Sales")
            )
        }

        binding.tvViewAllNew.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("home_filter" to "News")
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

        // todo fetch list image and title
        val imgs = listOf<String>(
            "https://image.uniqlo.com/UQ/ST3/vn/imagesgoods/448970/item/vngoods_09_448970.jpg?width=1600&impolicy=quality_75",
            "https://image.uniqlo.com/UQ/ST3/vn/imagesgoods/448970/item/vngoods_09_448970.jpg?width=1600&impolicy=quality_75",
            "https://image.uniqlo.com/UQ/ST3/vn/imagesgoods/448970/item/vngoods_09_448970.jpg?width=1600&impolicy=quality_75"
        )
        binding.topAppBar.viewPager.adapter = ImageProductDetailAdapter(imgs)
        binding.topAppBar.viewPager.autoScroll(3500)
    }

}
