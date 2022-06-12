package com.goldenowl.ecommerce.ui.global.home

import android.app.SearchManager
import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.AppBarCategoryListAdapter
import com.goldenowl.ecommerce.adapter.CategoryProductListAdapter
import com.goldenowl.ecommerce.databinding.FragmentCategoryBinding
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertFavorite
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetSortProduct
import com.goldenowl.ecommerce.ui.global.favorites.FavoritesFragment
import com.goldenowl.ecommerce.utils.Consts
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel


class CategoryFragment : BaseHomeFragment<FragmentCategoryBinding>() {

    private lateinit var adapterGrid: CategoryProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager


    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var filterType: String? = null
    private var sortType: SortType? = null


    //    private val sortViewModel: SortFilterViewModel by viewModels()
    private val sortViewModel = SortFilterViewModel()
    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) {
            listProductData = it
            adapterGrid.setData(listProductData, filterType, sortType)
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: allFavorite change")
            viewModel.reloadListProductData()
        }

        sortViewModel.filterType.observe(viewLifecycleOwner) {
            if (it == null) binding.topAppBar.collapsingToolbar.title = "All products"
            else {
                filterType = it
                binding.topAppBar.collapsingToolbar.title = it
                adapterGrid.setData(listProductData, filterType, sortType)
            }
        }

        sortViewModel.sortType.observe(viewLifecycleOwner) {
            sortType = it
            binding.topAppBar.tvSort.text = getString(Consts.sortMap[it] ?: R.string.none)
            adapterGrid.setData(listProductData, filterType, sortType)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun init() {
        listCategory = viewModel.categoryList
        Log.d(TAG, "init: listCategory=$listCategory")
        listProductData = viewModel.listProductData.value ?: emptyList()
    }


    override fun setViews() {
        /*list view*/


        gridLayoutManager = GridLayoutManager(context, Consts.SPAN_COUNT_ONE)
        adapterGrid = CategoryProductListAdapter(gridLayoutManager, object : CategoryProductListAdapter.IClickListener {
            override fun onClickFavorite(product: Product, favorite: Favorite?) {
                Log.d(TAG, "onClickFavorite: $favorite")
                if (favorite == null) {
                    Log.d(FavoritesFragment.TAG, "onClickFavorite: insert favorite")
                    toggleBottomSheetAddToFavorite(product)
                } else {
                    Log.d(FavoritesFragment.TAG, "onClickFavorite: remove favorite")
                    viewModel.removeFavorite(favorite!!)
                }
            }

        })
        val homeFilter = arguments?.getString("home_filter")
        sortViewModel.filterType.value = homeFilter

        binding.rcvCategoryGrid.adapter = adapterGrid
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

        binding.topAppBar.ivViewType.setOnClickListener {
            switchLayout()
        }
        binding.topAppBar.layoutSort.setOnClickListener {
            toggleBottomSheetSortProduct()
        }
    }

    private fun switchLayout() {
        gridLayoutManager.apply {
            spanCount = if (spanCount == Consts.SPAN_COUNT_ONE) {
                binding.topAppBar.ivViewType.setImageResource(R.drawable.ic_list)
                Consts.SPAN_COUNT_TWO
            } else {
                binding.topAppBar.ivViewType.setImageResource(R.drawable.ic_grid)
                Consts.SPAN_COUNT_ONE
            }
        }
        adapterGrid.notifyDataSetChanged()
    }

    private fun toggleBottomSheetSortProduct() {
        val modalBottomSheet = BottomSheetSortProduct(sortViewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetSortProduct.TAG)
    }


    private fun toggleBottomSheetAddToFavorite(product: Product) {
        val modalBottomSheet = BottomSheetInsertFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetInsertFavorite.TAG)
    }


    override fun setAppbar() {
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            sortViewModel.filterType.value = null
            findNavController().navigateUp()
        }
        setAppBarMenu()

        val listCategory = binding.topAppBar.listCategory

        val list = getListCategory()

        listCategory.adapter =
                //todo
            AppBarCategoryListAdapter(
                list,
                object : AppBarCategoryListAdapter.IClickListener {
                    override fun onClick(position: Int) {
                        sortViewModel.filterType.value = list[position]
                    }

                })
        listCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    }


    private fun setAppBarMenu() {
        binding.topAppBar.toolbar.apply {
            inflateMenu(R.menu.menu_search)
            val searchItem = menu.findItem(R.id.ic_search)
            val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

            if (searchItem != null) {
                searchView = searchItem.actionView as SearchView
            }
            if (searchView != null) {
//                searchView!!.setIconifiedByDefault(true)
                searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
                queryTextListener = object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.i("onQueryTextChange", newText!!)
                        return true
                    }

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Log.i("onQueryTextSubmit", query!!)
//                        viewModel.searchProducts(query)
                        binding.topAppBar.toolbar.collapseActionView()
                        Log.d(
                            TAG,
                            "onQueryTextSubmit: hasactionview=${binding.topAppBar.toolbar.hasExpandedActionView()}"
                        )
                        return true
                    }
                }
                searchView!!.setOnCloseListener {
//                    binding.topAppBar.collapsingToolbar.hide
                    Log.d(TAG, "setAppBarMenu: closed")
                    false
                }
                searchView!!.maxWidth = Integer.MAX_VALUE
                searchView!!.setOnQueryTextListener(queryTextListener)
            } else {
                Log.d(TAG, "onCreateOptionsMenu: SEARCH VIEW NULL")
            }
        }

        //todo
        binding.topAppBar.toolbar.setOnMenuItemClickListener {
            onMenuClick(it)
        }
    }

    private fun onMenuClick(menuItem: MenuItem?): Boolean {
        Log.d(TAG, "onMenuClick: ${menuItem?.itemId}")
        when (menuItem?.itemId) {
            R.id.ic_search -> {
                Log.d(TAG, "onMenuClick: search clicked")
                // todo
//                binding.topAppBar.searchBar.searchBarFrameLayout.apply {
//                    visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.INVISIBLE
//                }
                return false
            }
        }
        return false
    }

    private fun getListCategory(): List<String> {
        return viewModel.categoryList.toList()
    }


    companion object {
        const val TAG = "CategoryFragment"
    }

    override fun getViewBinding(): FragmentCategoryBinding {
        return FragmentCategoryBinding.inflate(layoutInflater)
    }

}


