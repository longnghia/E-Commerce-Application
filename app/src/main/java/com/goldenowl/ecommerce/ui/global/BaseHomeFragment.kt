package com.goldenowl.ecommerce.ui.global

import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.viewmodels.ShopViewModel
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory

abstract class BaseHomeFragment<VBinding : ViewBinding> : BaseFragment<VBinding>() {

    protected val viewModel: ShopViewModel by activityViewModels {
        ProductViewModelFactory(
            (requireActivity().application as MyApplication).productsRepository,
            (requireActivity().application as MyApplication).authRepository
        )
    }
}

