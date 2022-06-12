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
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertFavorite
import com.goldenowl.ecommerce.ui.global.favorites.FavoritesFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
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
                Log.d(CategoryFragment.TAG, "setObservers: allFavorite change")
                viewModel.reloadListProductData()
            }
            viewModel.toastMessage.observe(viewLifecycleOwner) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun setViews() {
        Log.d(TAG, "setViews: started")
        val listener = object : HomeProductListAdapter.IClickListener {
            override fun onClickFavorite(product: Product, favorite: Favorite?) {
                Log.d(CategoryFragment.TAG, "onClickFavorite: $favorite")
                if (favorite == null) {
                    Log.d(FavoritesFragment.TAG, "onClickFavorite: insert favorite")
                    toggleBottomSheetAddToFavorite(product)
                } else {
                    Log.d(FavoritesFragment.TAG, "onClickFavorite: remove favorite")
                    viewModel.removeFavorite(favorite!!)
                }
            }
        }
        salesListAdapter = HomeProductListAdapter(listener)
        newsListAdapter = HomeProductListAdapter(listener)
        binding.rcvSales.adapter = salesListAdapter
        binding.rcvSales.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvNew.adapter = newsListAdapter
        binding.rcvNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvViewAllSale.setOnClickListener {
            Log.d(TAG, "setViews: View all clicked")
            findNavController().navigate(
                R.id.action_view_all,
                bundleOf("home_filter" to "Sale")
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
    }
    private fun toggleBottomSheetAddToFavorite(product: Product) {
        val modalBottomSheet = BottomSheetInsertFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetInsertFavorite.TAG)
    }
}