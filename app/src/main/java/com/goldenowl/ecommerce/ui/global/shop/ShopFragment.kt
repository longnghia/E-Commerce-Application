package com.goldenowl.ecommerce.ui.global.shop

import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentShopBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils


class ShopFragment : BaseHomeFragment<FragmentShopBinding>() {

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
                findNavController().navigate(
                    R.id.category_dest,
                    bundleOf(Constants.KEY_CATEGORY to viewModel.categoryList.elementAt(position))
                )
            }

            binding.btnViewAll.setOnClickListener {
                findNavController().navigate(
                    R.id.action_go_category
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
                inflateMenu(R.menu.menu_search_icon)
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.ic_search) {
                        findNavController().navigate(R.id.search_dest)
                    }
                    false
                }
            }
        }
    }
}
