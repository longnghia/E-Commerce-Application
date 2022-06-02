package com.goldenowl.ecommerce.ui.global.shop

import android.app.SearchManager
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentShopBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.ui.global.home.CategoryFragment
import com.goldenowl.ecommerce.utils.Utils


class ShopFragment : BaseHomeFragment<FragmentShopBinding>() {
    private var searchView: SearchView? = null

    override fun getViewBinding(): FragmentShopBinding {
        return FragmentShopBinding.inflate(layoutInflater)
    }

    override fun setViews() {
//        binding.rcvListCategory.adapter = ShopCategoryAdapter(getListCategory())
        val list = viewModel.categoryList.toList()
        Log.d(TAG, "setViews: list = $list")
//        for (i in list.indices) {
//            val textView = TextView(context).apply {
//                text = list[i]
//                setOnClickListener {
//                    findNavController().navigate(
//                        R.id.category_dest,
//                        bundleOf("category" to i)
//                    )
//                }
//            }
////            binding.listCategory.addView(textView)
////            binding.rcvListCategory.addView(textView)
//            binding.listCategories.addView(textView)
//        }
//        val dividerItemDecoration = DividerItemDecoration(
//            context, LinearLayout.HORIZONTAL
//        )
//        binding.rcvListCategory.addItemDecoration(dividerItemDecoration)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        binding.listCategory.apply {
            this.adapter = adapter
            setOnItemClickListener { _, _, position, id ->
                Log.d(TAG, "setViews: navigate to ${list[position]}")
                viewModel.currentCategory.value = position
                findNavController().navigate(
                    R.id.category_dest,
                    bundleOf("category" to position)
                )
            }

            binding.btnViewAll.setOnClickListener {
                viewModel.currentCategory.value = -1

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
        if (binding.topAppBar.toolbar.menu.isEmpty()) {
            binding.topAppBar.toolbar.inflateMenu(R.menu.menu_search)
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
                    val queryTextListener = object : SearchView.OnQueryTextListener {
                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.i("onQueryTextChange", newText!!)
                            return true
                        }

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            Log.i("onQueryTextSubmit", query!!)
//                        viewModel.searchProducts(query)
                            binding.topAppBar.toolbar.collapseActionView()
                            Log.d(
                                CategoryFragment.TAG,
                                "onQueryTextSubmit: hasactionview=${binding.topAppBar.toolbar.hasExpandedActionView()}"
                            )
                            return true
                        }
                    }
                    searchView!!.setOnCloseListener {
//                    binding.topAppBar.collapsingToolbar.hide
                        Log.d(CategoryFragment.TAG, "setAppBarMenu: closed")
                        false
                    }
                    searchView!!.maxWidth = Integer.MAX_VALUE
                    searchView!!.setOnQueryTextListener(queryTextListener)
                } else {
                    Log.d(CategoryFragment.TAG, "onCreateOptionsMenu: SEARCH VIEW NULL")
                }
            }
        }
    }

    companion object {
        val TAG = "ShopFragment"
    }
}
