package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeProductListAdapter
import com.goldenowl.ecommerce.databinding.FragmentProductDetailBinding
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Utils.autoScroll


class ProductDetailFragment : BaseHomeFragment<FragmentProductDetailBinding>() {

    private lateinit var relateProductAdapter: HomeProductListAdapter
    private lateinit var product: Product
    private lateinit var productData: ProductData

    private var favorite: Favorite? = null
    private var relateList: List<ProductData> = emptyList()

    private var listProductData: List<ProductData> = emptyList()

    override fun setObservers() {
        viewModel.listProductData.observe(viewLifecycleOwner) { it ->
            Log.d(TAG, "setObservers: listProductData changed")
            listProductData = it
            listProductData.find {
                it.product.id == product.id
            }.also {
                favorite = it?.favorite
                Log.d(TAG, "setObservers: current favorite = $favorite")
                if (favorite != null)
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_selected)
                else
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_bold)
            }
            relateList = viewModel.getRelateProducts(product.tags)
            relateProductAdapter.setData(relateList, null)
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: allFavorite change")
            viewModel.reloadListProductData()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun init() {
        productData = arguments?.get("product_data") as ProductData
        product = productData.product
    }


    override fun setViews() {

        with(binding) {

            productDescription.text = product.description
            productRatingBar.rating = product.reviewStars.toFloat()
            tvNumberReviews.text = product.numberReviews.toString()
            productBrand.text = product.brandName
            productPrice.text = product.getDiscountPrice().toString() + "$"
            productTitle.text = product.title
        }

        /* drop down menu*/
        val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (binding.menuSize.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        (binding.menuColor.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        /* viewpager*/
        binding.viewPager.adapter = ImageProductDetailAdapter(product.images)
        binding.viewPager.autoScroll(3500)
        /* recyclerView*/
        relateProductAdapter = HomeProductListAdapter(this)
        val relateProducts = getRelateProducts()
        relateProductAdapter.setData(relateProducts, null)
        binding.rcvProducts.adapter = relateProductAdapter
        binding.rcvProducts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvNumItem.text = relateProducts.size.toString()

        binding.menuSize.editText?.doAfterTextChanged {
            Log.d(TAG, "setViews: doAfterTextChanged=${it.toString()}")
        }
        binding.menuColor.editText?.doAfterTextChanged {
            Log.d(TAG, "setViews: doAfterTextChanged=${it.toString()}")
        }
        binding.btnAddToCart.setOnClickListener {
            toggleBottomSheetInsertCart(product)
        }
        /* insert favorite btn */
        binding.ivFavorite.setOnClickListener {
            Log.d(TAG, "onClickFavorite: $favorite")
            if (favorite == null) {
                Log.d(TAG, "onClickFavorite: insert favorite")
                toggleBottomSheetInsertFavorite(product)
            } else {
                Log.d(TAG, "onClickFavorite: remove favorite")
                viewModel.removeFavorite(favorite!!)
            }
        }
    }

    private fun getRelateProducts(): List<ProductData> {
        return viewModel.getRelateProducts(product.tags)
    }


    override fun setAppbar() {
        binding.topAppBar.toolbar.apply {

            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener {
                //todo share product
                Log.d(TAG, "setAppbar: ${it.itemId == R.drawable.ic_share} ")
                false
            }
            title = product.categoryName
        }
    }


    private fun onMenuClick(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.ic_search -> {
                // todo
//                binding.topAppBar.searchBar.searchBarFrameLayout.apply {
//                    visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.INVISIBLE
//                }
                return false
            }
        }
        return false
    }

    private fun getListCategory(): List<String> {
        return viewModel.categoryList.toList()
    }


    companion object {
        const val TAG = "ProductDetailFragment"
    }

    override fun getViewBinding(): FragmentProductDetailBinding {
        return FragmentProductDetailBinding.inflate(layoutInflater)
    }
}


