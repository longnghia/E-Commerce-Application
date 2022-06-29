package com.goldenowl.ecommerce.ui.global.shop

import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentShopBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
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
        binding.topAppBar.toolbar.setOnMenuItemClickListener {
            Log.d(TAG, "setAppBarMenu: ${it.itemId}")
            if (it.itemId == R.id.ic_search) {

                binding.topAppBar.inputLayoutSearch.visibility = View.VISIBLE
                Log.d(TAG, "setAppBarMenu: search clicked")
            }
            false
        }

        binding.topAppBar.inputLayoutSearch.setEndIconOnClickListener {
            findNavController().navigate(R.id.qr_dest)
        }
    }


    private fun setAppBarMenu() {
        val toolBar = binding.topAppBar.toolbar
        if (toolBar.menu.isEmpty()) {
            toolBar.apply {
                inflateMenu(R.menu.menu_search_input)
            }
        }
    }

    companion object {
        val TAG = "ShopFragment"
    }
}
