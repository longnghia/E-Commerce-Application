package com.goldenowl.ecommerce.ui.global.shop

import android.app.SearchManager
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentShopBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Utils


class ShopFragment : BaseHomeFragment<FragmentShopBinding>() {
    private var searchView: SearchView? = null

    override fun getViewBinding(): FragmentShopBinding {
        return FragmentShopBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: ArrayAdapter<String>

    override fun setViews() {
        val fullList = viewModel.categoryList.toList()
        val list = fullList.toList()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        binding.listCategory.apply {
            adapter = this@ShopFragment.adapter
            setOnItemClickListener { _, _, position, _ ->
                val index = fullList.indexOf(list[position])
                findNavController().navigate(
                    R.id.category_dest
                )
            }

            binding.btnViewAll.setOnClickListener {

                findNavController().navigate(
                    R.id.action_view_all
                )
            }
        }
    }

    override fun setObservers() {

    }

    override fun setAppbar() {
        setAppBarMenu()
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.category)
        binding.topAppBar.appBarLayout.setBackgroundColor(
            Utils.getColor(requireContext(), R.color.background_color) ?: 0xF9F9F9
        )
    }


    private fun setAppBarMenu() {
        val toolBar = binding.topAppBar.toolbar

        if (toolBar.menu.isEmpty()) {
            toolBar.apply {
                inflateMenu(R.menu.menu_search)
            }
            setSearchView()
        }

        /* todo: not work but refresh category array*/
        if (searchView != null) {
            searchView!!.setQuery("", false);
        }
    }

    private fun setSearchView() {
        val fullList = viewModel.categoryList.toList()

        val menu = binding.topAppBar.toolbar.menu
        val searchItem = menu.findItem(R.id.ic_search)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }

        val queryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                Log.i("onQueryTextChange", newText!!)
//                filterCategory(fullList, newText)
                //todo uiscope
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i("onQueryTextSubmit", query!!)
                /* close on submit */
                searchView!!.apply {
                    clearFocus()
//                            visibility = View.GONE
                }

                return false
            }
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

            searchView!!.setOnCloseListener {
                Log.d(TAG, "setAppBarMenu: closed")
                false
            }
            searchView!!.maxWidth = Integer.MAX_VALUE
            searchView!!.setOnQueryTextListener(queryTextListener)
        }
    }

    companion object {
        val TAG = "ShopFragment"
    }
}
