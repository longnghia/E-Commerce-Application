package com.goldenowl.ecommerce.ui.global.shop

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSearchBinding
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager


class SearchFragment : BaseHomeFragment<FragmentSearchBinding>() {

    private lateinit var settingsManager: SettingsManager
    private var listCategory: List<String> = emptyList()
    private lateinit var listHistory: MutableList<String>

    private lateinit var historyAdapter: HistoryAdapter

    override fun init() {
        super.init()
        settingsManager = SettingsManager(requireContext())
        listHistory =
            settingsManager.getListHistory()?.toMutableList() ?: mutableListOf()
        listCategory = viewModel.categoryList.toList()
    }

    override fun getViewBinding(): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(layoutInflater)
    }

    override fun setAppbar() {
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setViews() {
        /*history*/
        if (listHistory.isEmpty()) {
            binding.layoutHistory.visibility = View.GONE
        }
        historyAdapter = HistoryAdapter(listHistory) {
            findProduct(it)
        }
        binding.rcvHistory.adapter = historyAdapter
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        binding.rcvHistory.layoutManager = layoutManager

        /*category*/
        binding.rcvCategoryGrid.adapter = CategoryAdapter(listCategory) { category ->
            findNavController().navigate(R.id.category_dest, bundleOf(Constants.KEY_CATEGORY to category))
        }

        if (listCategory.size <= CategoryAdapter.ITEM_COUNT)
            binding.tvViewAll.visibility = View.GONE

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.shop_dest)
        }

        binding.topAppBar.edtSearch.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = view.text.toString()
                if (text.isBlank())
                    return@setOnEditorActionListener false
                addHistory(text)
                historyAdapter.refresh(listHistory)
                findProduct(text)
                true
            }
            false
        }

        binding.topAppBar.inputLayoutSearch.setEndIconOnClickListener {
            findNavController().navigate(R.id.qr_dest)
        }

        binding.topAppBar.edtSearch.requestFocus()
    }

    private fun findProduct(query: String) {
        findNavController().navigate(R.id.category_dest, bundleOf(Constants.KEY_SEARCH to query))
        hideKeyboard()
    }

    override fun setObservers() {
    }

    private fun addHistory(history: String) {
        if (binding.layoutHistory.visibility != View.VISIBLE)
            binding.layoutHistory.visibility = View.VISIBLE
        if (listHistory.size >= Constants.HISTORY_SIZE)
            listHistory.removeAt(listHistory.size - 1)
        if (history !in listHistory) {
            listHistory.add(0, history)
            settingsManager.setListHistory(listHistory)
        }
    }
}
