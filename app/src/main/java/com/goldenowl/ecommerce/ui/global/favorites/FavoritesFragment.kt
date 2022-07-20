package com.goldenowl.ecommerce.ui.global.favorites

import android.app.SearchManager
import android.content.Context
import android.content.Intent
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
import com.goldenowl.ecommerce.databinding.FragmentFavoritesBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.auth.LoginSignupActivity
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetSortProduct
import com.goldenowl.ecommerce.ui.global.home.CategoryFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.sortMap
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.FavoriteProductListAdapter
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*

class FavoritesFragment : BaseHomeFragment<FragmentFavoritesBinding>() {
    override fun getViewBinding(): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(layoutInflater)
    }

    //        private val sortViewModel: SortFilterViewModel by activityViewModels()
//    private lateinit var sortViewModel: SortFilterViewModel
//    private val sortViewModel: SortFilterViewModel by viewModels()
    private val sortViewModel = SortFilterViewModel()


    private lateinit var adapterGrid: FavoriteProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var filterType: String? = null
    private var sortType: SortType? = null
    private var searchTerm: String = ""


    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null


    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) {
            listProductData = it
            refreshList()
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }
        viewModel.allCart.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        sortViewModel.filterType.observe(viewLifecycleOwner) {
            if (it == null) binding.topAppBar.collapsingToolbar.title = "My Favorites"
            else {
                filterType = it
                binding.topAppBar.collapsingToolbar.title = it
                refreshList()
            }
        }

        sortViewModel.sortType.observe(viewLifecycleOwner) {
            sortType = it
            binding.topAppBar.tvSort.text = getString(sortMap[it] ?: R.string.none)
            refreshList()
        }

        sortViewModel.searchTerm.observe(viewLifecycleOwner) {
            searchTerm = it
            refreshList()
        }
    }

    private fun refreshList() {
        adapterGrid.setData(listProductData, filterType, sortType, searchTerm)
    }


    override fun init() {
        listCategory = viewModel.categoryList
        listProductData = viewModel.listProductData.value ?: emptyList()
    }

    override fun setViews() {
        if (!viewModel.isLoggedIn()) {
            binding.layoutContent.visibility = View.GONE
            binding.topAppBar.appBarExtra.visibility = View.GONE
            binding.layoutNotLogIn.layoutNotLogIn.visibility = View.VISIBLE
            binding.layoutNotLogIn.layoutLogInToContinue.setOnClickListener {
                startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            }
        }
        gridLayoutManager = GridLayoutManager(context, Constants.SPAN_COUNT_ONE)
        adapterGrid = FavoriteProductListAdapter(gridLayoutManager, this)

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
            spanCount = if (spanCount == Constants.SPAN_COUNT_ONE) {
                binding.topAppBar.ivViewType.setImageResource(R.drawable.ic_list)
                Constants.SPAN_COUNT_TWO
            } else {
                binding.topAppBar.ivViewType.setImageResource(R.drawable.ic_grid)
                Constants.SPAN_COUNT_ONE
            }
        }
        adapterGrid.notifyDataSetChanged()
    }

    private fun toggleBottomSheetSortProduct() {
        val modalBottomSheet = BottomSheetSortProduct(sortViewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetSortProduct.TAG)
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
                                    Log.i("onQueryTextChange", newText!!)
                                    Log.d(CategoryFragment.TAG, "onQueryTextChange: uiScope")
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
//                    binding.topAppBar.collapsingToolbar.hide
                    false
                }
                searchView!!.maxWidth = Integer.MAX_VALUE
                searchView!!.setOnQueryTextListener(queryTextListener)
            }
        }

        //todo
        binding.topAppBar.toolbar.setOnMenuItemClickListener {
            onMenuClick(it)
        }
    }

    private fun onMenuClick(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.ic_search -> {
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
        const val TAG = "FavoriteFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            sortViewModel.filterType.value = null
            sortViewModel.sortType.value = null
            findNavController().navigateUp()
        }
        setAppBarMenu()

        val appbarListCategory = binding.topAppBar.listCategory
        appbarListCategory.adapter =
                //todo
            AppBarCategoryListAdapter(
                getListCategory(),
                object : AppBarCategoryListAdapter.IClickListenerAppbar {
                    override fun onClick(position: Int) {
                        sortViewModel.filterType.value = listCategory.elementAt(position)
                    }
                })
        appbarListCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
}





