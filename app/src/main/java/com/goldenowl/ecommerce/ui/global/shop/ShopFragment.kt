package com.goldenowl.ecommerce.ui.global.shop

import android.app.SearchManager
import android.content.Context
import android.os.Handler
import android.os.Looper
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

    //    private lateinit var adapter: ShopCategoryAdapter
    private lateinit var adapter: ArrayAdapter<String>
    private val mMainHandler = Handler(Looper.getMainLooper())

    override fun setViews() {
        val fullList = viewModel.categoryList.toList()
        val list = fullList.toList()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
//        adapter = ShopCategoryAdapter(requireContext(), list)
        Log.d(TAG, "setViews: count=${adapter.count}")
        binding.listCategory.apply {
            adapter = this@ShopFragment.adapter
            setOnItemClickListener { _, _, position, _ ->
                val index = fullList.indexOf(list[position])
//                viewModel.currentCategory.value = index
                findNavController().navigate(
                    R.id.category_dest
                )
            }

            binding.btnViewAll.setOnClickListener {
//                viewModel.currentCategory.value = -1

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
            Log.d(TAG, "setAppBarMenu: menu empty, creating")
            toolBar.apply {
                inflateMenu(R.menu.menu_search)
            }
            setSearchView()
        } else {
            Log.d(TAG, "setAppBarMenu: menu not empty")
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

    private fun filterCategory(fullList: List<String>, newText: String) {
        val list = fullList.filter { it.indexOf(newText, ignoreCase = true) >= 0 }

        mMainHandler.removeCallbacksAndMessages(null)
        mMainHandler.postDelayed({
            adapter.clear() // todo gently change data //TODO  Operation is not supported for read-only collection
            adapter.addAll(list)
            adapter.notifyDataSetChanged()
            Log.d(TAG, "setAppBarMenu: start filter count=${adapter.count}")

        }, 500)
    }

    companion object {
        val TAG = "ShopFragment"
    }
}
