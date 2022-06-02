package com.goldenowl.ecommerce.ui.global.home

import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentCategoryBinding
import com.goldenowl.ecommerce.databinding.ModalBottomSheetSortProductBinding
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.viewmodels.ProductViewModel
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory
import com.goldenowl.ecommerce.viewmodels.SortType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    private lateinit var products: List<Product>
    private val map = mapOf(
        SortType.POPULAR to R.string.sort_by_popular,
        SortType.NEWEST to R.string.sort_by_newest,
        SortType.PRICE_INCREASE to R.string.sort_by_price_low_2_high,
        SortType.PRICE_DECREASE to R.string.sort_by_price_high_2_low,
        SortType.REVIEW to R.string.sort_by_customer_review
    )
    private lateinit var adapterList: CategoryProductListAdapter
    private lateinit var adapterGrid: HomeProductListAdapter

    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
    }

    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)

        init()
        setViews()
        setObservers()

        return binding.root
    }

    private fun setObservers() {
        viewModel.currentCategory.observe(viewLifecycleOwner) {
            if (it < 0) {

            } else {
                Log.d(TAG, "setObservers: currentCategory=$it: ${viewModel.categoryList.elementAt(it)}")
                viewModel.setFilterProducts(it)
                binding.topAppBar.collapsingToolbar.title = viewModel.categoryList.elementAt(it).toString() //todo
            }
        }

        viewModel.filterProducts.observe(viewLifecycleOwner) {
            products = it
            Log.d(TAG, "setObservers: ${it.size} products")
            Log.d(TAG, "setObservers: filterProducts changed: $products")
            reLoadAdapter()
        }

        viewModel.sortType.observe(viewLifecycleOwner) {
            binding.topAppBar.tvSort.text = getString(map[it]!!)
        }
    }

    private fun reLoadAdapter() {

        adapterList.setData(products)
        adapterGrid.setData(products)
    }

    private fun init() {
        products = viewModel.filterProducts.value!!
        arguments?.getInt("category").let {
            Log.d(TAG, "init: $it")
            if (it != null) viewModel.currentCategory.value == it
        }

//        products = arguments?.get("products") as List<Product>
//        Log.d(TAG, "onCreateView: received products: $products")
    }

    private fun setViews() {
        setAppBar()

        /*list view*/
        adapterList = CategoryProductListAdapter()
        adapterList.setData(products)
        binding.rcvCategory.adapter = adapterList
        val linearLayoutManager = LinearLayoutManager(context)
        binding.rcvCategory.layoutManager = linearLayoutManager

        /*grid view*/
        adapterGrid = HomeProductListAdapter(true)
        adapterGrid.setData(products)
        binding.rcvCategoryGrid.adapter = adapterGrid
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

        binding.topAppBar.ivViewType.setOnClickListener {
            toggleGridView()
        }
        binding.topAppBar.layoutSort.setOnClickListener {
            toggleBottomSheet()
        }
    }

    private fun toggleBottomSheet() {
        val modalBottomSheet = SortModalBottomSheet()
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, SortModalBottomSheet.TAG)
    }

    private fun toggleGridView() {
        if (binding.rcvCategory.visibility == View.VISIBLE) {
            binding.rcvCategory.visibility = View.GONE
            binding.rcvCategoryGrid.visibility = View.VISIBLE
        } else {
            binding.rcvCategory.visibility = View.VISIBLE
            binding.rcvCategoryGrid.visibility = View.GONE
        }
    }

    private fun setAppBar() {

        binding.topAppBar.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        setAppBarMenu()

        val listCategory = binding.topAppBar.listCategory
        listCategory.adapter =
            //todo
            AppBarCategoryListAdapter(getListCategory(), viewModel.currentCategory.value!! ,object : AppBarCategoryListAdapter.IClickListener {
                override fun onClick(position: Int) {
                    viewModel.currentCategory.value = position
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
                        viewModel.searchProducts(query)
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
}

class SortModalBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
    }
    private lateinit var binding: ModalBottomSheetSortProductBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetSortProductBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = mapOf<SortType, TextView>(
            SortType.POPULAR to binding.sortByPopular,
            SortType.NEWEST to binding.sortByNewest,
            SortType.PRICE_INCREASE to binding.sortByPriceInsc,
            SortType.PRICE_DECREASE to binding.sortByPriceDesc,
            SortType.REVIEW to binding.sortByReview
        )

        for (pair in map) {
            val (type, view) = pair
            view.setOnClickListener {
                viewModel.setSortBy(type)
                viewModel.sortBy(type)
                dismiss()
            }
        }

        viewModel.sortType.observe(viewLifecycleOwner) {
            for (pair in map) {
                val (type, view) = pair
                if (type == it) {
                    setViewBackground(view, R.color.red_dark)
                } else {
                    view.setBackgroundColor(0)
                }

            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                map[it]?.setBackgroundColor(resources.getColor(R.color.red_dark, activity?.theme))
//            } else {
//                map[it]?.setBackgroundColor(resources.getColor(R.color.red_dark))
//            }
        }
    }

    private fun setViewBackground(v: TextView, c: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            v.setBackgroundColor(resources.getColor(c, activity?.theme))
            v.setTextColor(requireContext().getColor(R.color.white))
        } else {
            v.setBackgroundColor(resources.getColor(c))
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }


}