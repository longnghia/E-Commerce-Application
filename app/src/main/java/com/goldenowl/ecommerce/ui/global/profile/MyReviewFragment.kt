package com.goldenowl.ecommerce.ui.global.profile

import android.view.View
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentMyReviewBinding
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.viewmodels.MyReviewAdapter

class MyReviewFragment : BaseHomeFragment<FragmentMyReviewBinding>() {
    private lateinit var adapter: MyReviewAdapter
    override fun getViewBinding(): FragmentMyReviewBinding {
        return FragmentMyReviewBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        adapter = MyReviewAdapter()
        binding.rcvReviews.adapter = adapter

        refreshData()
        binding.checkWithPhoto.setOnClickListener {
            it.isSelected = !it.isSelected
            refreshData()
        }
    }

    private fun refreshData() {
        var list = viewModel.mListReview.value ?: emptyList()

        if (list.isEmpty()) {
            binding.tvNoReviewYet.visibility = View.VISIBLE
            binding.layoutReviewRating.visibility = View.INVISIBLE
        } else {
            binding.tvNoReviewYet.visibility = View.INVISIBLE
            binding.layoutReviewRating.visibility = View.VISIBLE
        }

        if (binding.checkWithPhoto.isSelected) {
            list = list?.filter { it.images.isNotEmpty() }
        }
        adapter.setData(list)
        binding.tvNumberReviews.text =
            resources.getQuantityString(R.plurals.num_review, list.size, list.size)
    }

    override fun setObservers() {

    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.my_review)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}