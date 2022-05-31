package com.goldenowl.ecommerce.ui.global.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.viewmodels.ProductViewModel
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory

class FavoritesFragment : Fragment() {
    private val viewModel: ProductViewModel by activityViewModels {
        ProductViewModelFactory((requireActivity().application as MyApplication).productsRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.fragment_favorites, container, false);
        return view;
    }
}