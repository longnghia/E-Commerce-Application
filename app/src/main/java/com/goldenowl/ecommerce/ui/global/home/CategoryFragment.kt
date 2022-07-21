package com.goldenowl.ecommerce.ui.global.home

import android.app.SearchManager
import android.content.Context
import android.util.Log
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
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetFilterProduct
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetSortProduct
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*
import java.util.*


class CategoryFragment : BaseHomeFragment<FragmentCategoryBinding>() {

    private lateinit var listCategoryAdapter: AppBarCategoryListAdapter
    private lateinit var adapterGrid: CategoryProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager


    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var filterType: String? = null
    private var sortType: SortType? = null
    private var searchTerm: String = ""
    private var filterPrice: List<Float>? = null


    private val sortViewModel = SortFilterViewModel()
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

        sortViewModel.filterType.observe(viewLifecycleOwner) {
            if (it == null) binding.topAppBar.collapsingToolbar.title = getString(R.string.all_product)
            else {
                filterType = it
                binding.topAppBar.collapsingToolbar.title = when(it){
                    Constants.KEY_NEW -> getString(R.string.news)
                    Constants.KEY_SALE -> getString(R.string.sales)
                    else -> it
                }
                binding.tvProductForQuery.visibility = View.GONE
                listCategory.indexOf(it).let { index ->
                    if (index > -1){
                        listCategoryAdapter.setPosition(index)
                        binding.topAppBar.listCategory.smoothScrollToPosition(index)
                    }
                }
                refreshList()
            }
        }

        sortViewModel.filterByPrice.observe(viewLifecycleOwner) {
            filterPrice = it
            binding.topAppBar.tvFilter.text = if (filterPrice != null) getString(R.string.filter_by_price) else
                getString(R.string.filters)
            refreshList()
        }

        sortViewModel.sortType.observe(viewLifecycleOwner) {
            sortType = it
            binding.topAppBar.tvSort.text = getString(Constants.sortMap[it] ?: R.string.none)
            refreshList()
        }

        sortViewModel.searchTerm.observe(viewLifecycleOwner) {
            if(it.isNullOrBlank())
                return@observe
            searchTerm = it
            binding.tvProductForQuery.text = getString(R.string.product_for, it)
            binding.tvProductForQuery.visibility = View.VISIBLE
            refreshList()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun init() {
        listCategory = viewModel.categoryList
        listProductData = viewModel.listProductData.value ?: emptyList()
        arguments?.apply {
            getString(Constants.KEY_SEARCH)?.let {
                sortViewModel.searchTerm.value = it
            }
        }
    }


    override fun setViews() {
        /*list view*/


        gridLayoutManager = GridLayoutManager(context, Constants.SPAN_COUNT_ONE)
        adapterGrid = CategoryProductListAdapter(gridLayoutManager, this)
        val homeFilter = arguments?.getString(Constants.KEY_CATEGORY)
        sortViewModel.filterType.value = homeFilter

        binding.rcvCategoryGrid.adapter = adapterGrid
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

        binding.topAppBar.ivViewType.setOnClickListener {
            switchLayout()
        }
        binding.topAppBar.layoutSort.setOnClickListener {
            toggleBottomSheetSortProduct()
        }
        binding.topAppBar.layoutFilter.setOnClickListener {
            toggleBottomSheetFilterProduct()
        }
    }

    private fun refreshList() {
        var filteredList = listProductData.toList()

        if (filterType == Constants.KEY_SALE)
            filteredList = filteredList.filter { it.product.salePercent != null }
        else if (filterType == Constants.KEY_NEW)
            filteredList = filteredList.filter { it.product.createdDate > Date(0) }
        else if (filterType != null)
            filteredList = filteredList.filter { it.product.categoryName == filterType }

        filteredList = when (sortType) {
            SortType.REVIEW -> filteredList.sortedByDescending { it.product.reviewStars }
            SortType.PRICE_DECREASE -> filteredList.sortedByDescending { it.product.getDiscountPrice() }
            SortType.PRICE_INCREASE -> filteredList.sortedBy { it.product.getDiscountPrice() }
            SortType.POPULAR -> filteredList.sortedByDescending { it.product.isPopular }
            SortType.NEWEST -> filteredList.sortedByDescending { it.product.createdDate }
            else -> filteredList
        }
        if (searchTerm.isNotBlank()) {
            filteredList = filteredList.filter {
                it.product.title.indexOf(searchTerm, ignoreCase = true) >= 0 || it.product.brandName.indexOf(
                    searchTerm,
                    ignoreCase = true
                ) >= 0
            }
        }
        filterPrice?.let { filter ->
            filteredList = filteredList.filter {
                val price = it.product.getDiscountPrice()
                price >= filter[0] && price <= filter[1]
            }
        }
        if (filteredList.isEmpty()) {
            binding.tvNoProduct.visibility = View.VISIBLE
            binding.layoutCategory.visibility = View.GONE
        } else {
            binding.tvNoProduct.visibility = View.GONE
            binding.layoutCategory.visibility = View.VISIBLE
            adapterGrid.setData(filteredList)
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

    private fun toggleBottomSheetFilterProduct() {

        val modalBottomSheet =
            BottomSheetFilterProduct(filterPrice, object : BottomSheetFilterProduct.OnFilterListener {
                override fun discard() {
                    sortViewModel.disCardFilterPrice()
                }

                override fun apply(values: List<Float>) {
                    sortViewModel.filterByPrice.value = values
                }

            })
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetFilterProduct.TAG)
    }

    override fun setAppbar() {
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            sortViewModel.filterType.value = null
            findNavController().navigateUp()
        }
        setAppBarMenu()

        val listCategory = binding.topAppBar.listCategory

        val list = getListCategory()

        listCategoryAdapter = AppBarCategoryListAdapter(
            list,
            object : AppBarCategoryListAdapter.IClickListenerAppbar {
                override fun onClick(position: Int) {
                    sortViewModel.filterType.value = list[position]
                }
            })
        listCategory.adapter = listCategoryAdapter

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
//                searchView!!.imeOptions = EditorInfo.IME_ACTION_DONE;
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


    private fun getListCategory(): List<String> {
        return viewModel.categoryList.toList()
    }

    override fun getViewBinding(): FragmentCategoryBinding {
        return FragmentCategoryBinding.inflate(layoutInflater)
    }
}


