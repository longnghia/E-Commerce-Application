package com.goldenowl.ecommerce.ui.global

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertCart
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertFavorite
import com.goldenowl.ecommerce.ui.global.favorites.FavoritesFragment
import com.goldenowl.ecommerce.ui.global.home.ProductDetailFragment
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory
import com.goldenowl.ecommerce.viewmodels.ShopViewModel

abstract class BaseHomeFragment<VBinding : ViewBinding> : BaseFragment<VBinding>(),
    IClickListener {

    protected val viewModel: ShopViewModel by activityViewModels {
        ProductViewModelFactory(
            (requireActivity().application as MyApplication).productsRepository,
            (requireActivity().application as MyApplication).authRepository
        )
    }

    override fun onClickFavorite(product: Product, favorite: Favorite?) {
        Log.d(ProductDetailFragment.TAG, "onClickFavorite: $favorite")
        if (favorite == null) {
            Log.d(ProductDetailFragment.TAG, "onClickFavorite: insert favorite")
            toggleBottomSheetInsertFavorite(product)
        } else {
            Log.d(ProductDetailFragment.TAG, "onClickFavorite: remove favorite")
            viewModel.removeFavorite(favorite!!)
        }
    }

    override fun onClickItem(productData: ProductData) {
        findNavController().navigate(
            R.id.detail_dest,
            bundleOf("product_data" to productData)
        )
    }

    override fun onClickCart(product: Product, cart: Cart?) {
        Log.d(FavoritesFragment.TAG, "onClickCart: $cart")
        if (cart == null) {
            toggleBottomSheetInsertCart(product)
        } else {
            viewModel.removeCart(cart)
        }

    }

    override fun onClickRemoveFavorite(product: Product, favorite: Favorite?) {
        if (favorite != null) {
            Log.d(FavoritesFragment.TAG, "onClickRemoveFavorite: remove from favorite")
            viewModel.removeFavorite(favorite)
        }
    }

    protected fun toggleBottomSheetInsertFavorite(product: Product) {
        val modalBottomSheet = BottomSheetInsertFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetInsertCart.TAG)
    }
    protected fun toggleBottomSheetInsertCart(product: Product) {
        val modalBottomSheet = BottomSheetInsertCart(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, ProductDetailFragment.TAG)
    }
}

interface IClickListener {
    fun onClickFavorite(product: Product, favorite: Favorite?)
    fun onClickItem(productData: ProductData)
    fun onClickCart(product: Product, cart: Cart?)
    fun onClickRemoveFavorite(product: Product, favorite: Favorite?)
}

