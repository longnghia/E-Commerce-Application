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
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetEnterPromo
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertCart
import com.goldenowl.ecommerce.ui.global.bottomsheet.BottomSheetInsertFavorite
import com.goldenowl.ecommerce.viewmodels.ProductViewModelFactory
import com.goldenowl.ecommerce.viewmodels.ShopViewModel

abstract class BaseHomeFragment<VBinding : ViewBinding> : BaseFragment<VBinding>(),
    IClickListener {

    private val TAG = "BaseHomeFragment"
    protected val viewModel: ShopViewModel by activityViewModels {
        ProductViewModelFactory(
            (requireActivity().application as MyApplication).productsRepository,
            (requireActivity().application as MyApplication).authRepository
        )
    }

    override fun onClickFavorite(product: Product, favorite: Favorite?) {
        Log.d(TAG, "onClickFavorite: $favorite")
        if (favorite == null) {
            Log.d(TAG, "onClickFavorite: insert favorite")
            toggleBottomSheetInsertFavorite(product)
        } else {
            Log.d(TAG, "onClickFavorite: remove favorite")
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
        Log.d(TAG, "onClickCart: $cart")
        if (cart == null) {
            toggleBottomSheetInsertCart(product, null)
        } else {
            viewModel.removeCart(cart)
        }

    }

    override fun onClickRemoveFavorite(product: Product, favorite: Favorite?) {
        if (favorite != null) {
            Log.d(TAG, "onClickRemoveFavorite: remove from favorite")
            viewModel.removeFavorite(favorite)
        }
    }

    override fun updateCart(cart: Cart) {
        viewModel.updateCart(cart)
    }
    protected fun toggleBottomSheetInsertFavorite(product: Product) {
        val modalBottomSheet = BottomSheetInsertFavorite(product, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, BottomSheetInsertCart.TAG)
    }

    /**
     * Open model bottom sheet to select size
     * @param cart if cart not null, mean user has chosen color and size in product detail screen
     */
    protected fun toggleBottomSheetInsertCart(product: Product, cart: Cart?) {
        val modalBottomSheet = BottomSheetInsertCart(product, cart, viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, TAG)
    }

    protected fun toggleBottomSheetEnterPromo(){
        val modalBottomSheet = BottomSheetEnterPromo(viewModel)
        modalBottomSheet.enterTransition = View.GONE
        modalBottomSheet.show(parentFragmentManager, TAG)
    }
}

interface IClickListener {
    fun onClickFavorite(product: Product, favorite: Favorite?)
    fun onClickItem(productData: ProductData)
    fun onClickCart(product: Product, cart: Cart?)
    fun onClickRemoveFavorite(product: Product, favorite: Favorite?)
    fun updateCart(cart: Cart)
}

