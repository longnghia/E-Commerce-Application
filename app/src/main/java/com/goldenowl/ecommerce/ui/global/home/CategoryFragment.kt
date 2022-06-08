package com.goldenowl.ecommerce.ui.global.home

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
import com.goldenowl.ecommerce.databinding.ModalBottomSheetAddToFavoriteBinding
import com.goldenowl.ecommerce.databinding.ModalBottomSheetSortProductBinding
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.goldenowl.ecommerce.viewmodels.SortType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding

    private lateinit var products: List<Product>
    private var favoriteList: List<UserOrder.Favorite> = ArrayList()

    private val map = mapOf(
        SortType.POPULAR to R.string.sort_by_popular,
        SortType.NEWEST to R.string.sort_by_newest,
        SortType.PRICE_INCREASE to R.string.sort_by_price_low_2_high,
        SortType.PRICE_DECREASE to R.string.sort_by_price_high_2_low,
        SortType.REVIEW to R.string.sort_by_customer_review
    )
    private lateinit var adapterList: CategoryProductListAdapter
    private lateinit var adapterGrid: HomeProductListAdapter

    private val viewModel: ShopViewModel by activityViewModels {
        ProductViewModelFactory(
            (requireActivity().application as MyApplication).productsRepository,
            (requireActivity().application as MyApplication).authRepository
        )
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
                binding.topAppBar.collapsingToolbar.title = "All products"
            } else {
                Log.d(TAG, "setObservers: currentCategory=$it: ${viewModel.categoryList.elementAt(it)}")
                viewModel.setFilterProducts(it)
                binding.topAppBar.collapsingToolbar.title = viewModel.categoryList.elementAt(it).toString()
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

        viewModel.favoriteList.observe(viewLifecycleOwner){
            Log.d(TAG, "setObservers: favorites changes: $it")
            favoriteList = it
        }
    }

    private fun reLoadAdapter() {
        adapterList.setData(products, favoriteList)
        adapterGrid.setData(products, favoriteList)
    }

    private fun init() {
        products = viewModel.filterProducts.value!!
    }

    private fun setViews() {
        setAppBar()

        /*list view*/
        adapterList = CategoryProductListAdapter(object : CategoryProductListAdapter.IClickListener {
            override fun onClickFavorite(product: Product) {
                toggleBottomSheetAddToFavorite(product)
            }

        })
        adapterList.setData(products, favoriteList)
        binding.rcvCategory.adapter = adapterList
        val linearLayoutManager = LinearLayoutManager(context)
        binding.rcvCategory.layoutManager = linearLayoutManager

        /*grid view*/
        adapterGrid = HomeProductListAdapter(true)
        adapterGrid.setData(products, favoriteList)
        binding.rcvCategoryGrid.adapter = adapterGrid
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.rcvCategoryGrid.layoutManager = gridLayoutManager

        binding.topAppBar.ivViewType.setOnClickListener {
            toggleGridView()
        }
        binding.topAppBar.layoutSort.setOnClickListener {
            toggleBottomSheetSortProduct()
        }
    }

    private fun toggleBottomSheetSortProduct() {
        val modalBottomSheet = BottomSheetSortProduct(viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetSortProduct.TAG)
    }


    private fun toggleBottomSheetAddToFavorite(product: Product) {
        val modalBottomSheet = BottomSheetAddToFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetAddToFavorite.TAG)
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
            AppBarCategoryListAdapter(
                getListCategory(),
                viewModel.currentCategory.value!!,
                object : AppBarCategoryListAdapter.IClickListener {
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

class BottomSheetSortProduct(private val viewModel: ShopViewModel) : BottomSheetDialogFragment() {
//    private val viewModel: ProductViewModel by activityViewModels {
//        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
//    }
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
        const val TAG = "BottomSheetSortProduct"
    }
}


class BottomSheetAddToFavorite(private val product: Product, private val viewModel: ShopViewModel) : BottomSheetDialogFragment() {
//    private val viewModel: ProductViewModel by activityViewModels {
//        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
//    }

    private lateinit var binding: ModalBottomSheetAddToFavoriteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ModalBottomSheetAddToFavoriteBinding.inflate(layoutInflater, container, false).apply {
            binding = this
        }.root

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val product = arguments?.get("product") as Product
        val mapString = mapOf(
            R.id.radio_size_L to "L",
            R.id.radio_size_M to "M",
            R.id.radio_size_S to "S",
            R.id.radio_size_XL to "XL",
            R.id.radio_size_XS to "XS"
        )
        val mapView = mapOf(
            "L" to binding.radioSizeL,
            "M" to binding.radioSizeM,
            "S" to binding.radioSizeS,
            "XL" to binding.radioSizeXL,
            "XS" to binding.radioSizeXS
        )

        if (product != null) {
            val listSize = product.getListSize()
            if (listSize.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: list size = $listSize")
                for (size in listSize) {
                    val s = size.size
                    val view = mapView[s] as RadioButton

                    val available = size.quantity > 0
                    setRadioButton(view, available)
                }
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            Log.d(TAG, "onViewCreated: checked id = $checkedId, size=${mapString[checkedId]}")
//            .setTextColor(requireContext().getColor(R.color.white))
        }

        binding.btnAddToFavorites.setOnClickListener {
            viewModel.addToFavorite(
                UserOrder.Favorite(
                    product.id,
                    mapString[binding.radioGroup.checkedRadioButtonId]!!,
                    "black"
                )
            )
        }

    }

    private fun setRadioButton(v: RadioButton, available: Boolean) {
        v.isEnabled = available
    }

    companion object {
        const val TAG = "BottomSheetAddToFavorite"
    }
}
