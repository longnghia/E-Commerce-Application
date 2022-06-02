package com.goldenowl.ecommerce.ui.global

import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.viewmodels.ProductViewModel
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory

abstract class BaseHomeFragment<VBinding : ViewBinding> : BaseFragment<VBinding>() {

    protected val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
    }
}

