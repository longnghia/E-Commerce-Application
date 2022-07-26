package com.goldenowl.ecommerce.ui.global.profile


import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.BottomSheetPromoAdapter
import com.goldenowl.ecommerce.databinding.FragmentMyPromoBinding
import com.goldenowl.ecommerce.models.data.Promo
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment

class MyPromoFragment : BaseHomeFragment<FragmentMyPromoBinding>() {
    override fun getViewBinding(): FragmentMyPromoBinding {
        return FragmentMyPromoBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        val bottomSheetAdapter = BottomSheetPromoAdapter(
            viewModel.listPromo.value ?: emptyList(),
            object : BottomSheetPromoAdapter.IClickListenerPromo {
                override fun onClick(promo: Promo) {
                    val bag = viewModel.curBag.value
                    bag?.promo = promo
                    viewModel.curBag.value = bag
                    findNavController().navigate(R.id.bag_dest)
                }
            })

        binding.modalBottomSheetEnterPromo.rcvPromo.adapter = bottomSheetAdapter
        binding.modalBottomSheetEnterPromo.rcvPromo.layoutManager = LinearLayoutManager(context)
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.my_promo)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setObservers() {
    }
}


