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
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.AppBarCategoryListAdapter
import com.goldenowl.ecommerce.adapter.CategoryProductListAdapter
import com.goldenowl.ecommerce.databinding.FragmentCategoryBinding
import com.goldenowl.ecommerce.databinding.ModalBottomSheetAddToFavoriteBinding
import com.goldenowl.ecommerce.databinding.ModalBottomSheetSortProductBinding
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.favorites.FavoritesFragment
import com.goldenowl.ecommerce.utils.Consts
import com.goldenowl.ecommerce.utils.SortType
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


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
            Log.d(TAG, "Observers: listProductData=$listProductData")
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
        val modalBottomSheet = BottomSheetAddToFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetAddToFavorite.TAG)
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


class BottomSheetSortProduct(private val viewModel: SortFilterViewModel) : BottomSheetDialogFragment() {
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
                viewModel.setSortType(type)
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

class BottomSheetAddToFavorite(private val product: Product, private val viewModel: ShopViewModel) :
    BottomSheetDialogFragment() {
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
            viewModel.insertFavorite(
                Favorite(
                    product.id,
                    mapString[binding.radioGroup.checkedRadioButtonId]!!,
                    "black"
                )
            )
            dismiss()
        }

    }

    private fun setRadioButton(v: RadioButton, available: Boolean) {
        v.isEnabled = available
    }

    companion object {
        const val TAG = "BottomSheetAddToFavorite"
    }
}
