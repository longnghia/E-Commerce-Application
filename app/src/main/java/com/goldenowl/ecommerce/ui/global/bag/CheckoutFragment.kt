package com.goldenowl.ecommerce.ui.global.bag

import android.app.SearchManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.GridLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.CheckoutDeliveryAdapter
import com.goldenowl.ecommerce.databinding.FragmentCheckoutBinding
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.home.CategoryFragment
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.viewmodels.BagProductListAdapter
import com.goldenowl.ecommerce.viewmodels.SortFilterViewModel
import kotlinx.coroutines.*

class CheckoutFragment : BaseHomeFragment<FragmentCheckoutBinding>() {
    override fun getViewBinding(): FragmentCheckoutBinding {
        return FragmentCheckoutBinding.inflate(layoutInflater)
    }

    private val sortViewModel = SortFilterViewModel()


    private lateinit var adapterGrid: BagProductListAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var listProductData: List<ProductData>
    private lateinit var listCategory: Set<String>
    private var searchTerm: String = ""


    private var searchView: SearchView? = null
    private var queryTextListener: SearchView.OnQueryTextListener? = null

    private var totalPrice = 0f

    override fun setObservers() {

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
        binding.rcvDelivery.adapter = CheckoutDeliveryAdapter()
    }

    private fun checkOut() {
        val listCartProductData = adapterGrid.getBag()
    }


    private fun setAppBarMenu() {
        binding.topAppBar.toolbar.apply {
            if (menu.isEmpty()) {
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
                        Log.d(TAG, "setAppBarMenu: closed")
                        false
                    }
                    searchView!!.maxWidth = Integer.MAX_VALUE
                    searchView!!.setOnQueryTextListener(queryTextListener)
                } else {
                    Log.d(TAG, "onCreateOptionsMenu: SEARCH VIEW NULL")
                }
            }
        }
    }


    companion object {
        const val TAG = "CheckoutFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.checkout)
        setAppBarMenu()
    }
}
