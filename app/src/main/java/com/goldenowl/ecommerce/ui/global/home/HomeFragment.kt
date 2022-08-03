package com.goldenowl.ecommerce.ui.global.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeAdapter
import com.goldenowl.ecommerce.adapter.HomeViewPagerAdapter
import com.goldenowl.ecommerce.databinding.FragmentHomeBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils.autoScroll
import com.goldenowl.ecommerce.utils.Utils.getColor
import com.goldenowl.ecommerce.utils.Utils.prepareForTwoWayPaging

class HomeFragment : BaseHomeFragment<FragmentHomeBinding>() {
    val TAG: String = "HomeFragment"


    private var listCategory: MutableList<String> = mutableListOf()
    private lateinit var homeAdapter: HomeAdapter

    private lateinit var viewPagerAdapter: HomeViewPagerAdapter

    private var listProductData: List<ProductData> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private var mListAppbarImg: List<Pair<String, String>> = emptyList()

    override fun setObservers() {
        super.setObservers()
        viewModel.dataReady.observe(viewLifecycleOwner) {
            if (it == BaseLoadingStatus.LOADING) {
                binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.INVISIBLE
                (requireActivity() as MainActivity).showNavBar(false)
            } else {
                binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
                binding.layoutContent.visibility = View.VISIBLE
                (requireActivity() as MainActivity).showNavBar(true)
            }

            listCategory = viewModel.categoryList.toMutableList().apply {
                add(0, Constants.KEY_SALE)
                add(1, Constants.KEY_NEW)
            }
            Log.d(TAG, "listCategory: $listCategory")

            viewModel.listProductData.observe(viewLifecycleOwner) {
                listProductData = it
                homeAdapter = HomeAdapter(this, listCategory) { category ->
                    findNavController().navigate(
                        R.id.action_view_all,
                        bundleOf(Constants.KEY_CATEGORY to category)
                    )
                }
                homeAdapter.setData(listProductData)
                binding.rcvCategory.adapter = homeAdapter
            }

            viewModel.allFavorite.observe(viewLifecycleOwner) {
                viewModel.reloadListProductData()
            }

            viewModel.listAppbarImg.observe(viewLifecycleOwner) { list ->
                mListAppbarImg = list
                if (list.isEmpty())
                    return@observe
                viewPagerAdapter.setData(list.prepareForTwoWayPaging())
                binding.topAppBar.viewPager.autoScroll(handler, Constants.AUTO_SCROLL)
            }
        }
    }


    override fun setViews() {
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

        viewPagerAdapter = HomeViewPagerAdapter { title ->
            findNavController().navigate(R.id.category_dest, bundleOf(Constants.KEY_CATEGORY to title))
        }
        binding.topAppBar.viewPager.adapter = viewPagerAdapter
    }

    override fun onPause() {
        super.onPause()
        handler.removeMessages(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(0)
    }
}
