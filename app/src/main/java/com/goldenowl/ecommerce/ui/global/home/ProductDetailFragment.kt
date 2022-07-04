package com.goldenowl.ecommerce.ui.global.home

import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.HomeProductListAdapter
import com.goldenowl.ecommerce.adapter.ImageProductDetailAdapter
import com.goldenowl.ecommerce.databinding.FragmentProductDetailBinding
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.Constants.listSize
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
            listProductData = it
            listProductData.find {
                it.product.id == product.id
            }.also {
                favorite = it?.favorite
                if (favorite != null)
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_selected)
                else
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorites_bold)
            }
            relateList = viewModel.getRelateProducts(product.tags)
            relateProductAdapter.setData(relateList, null)
        }

        viewModel.allFavorite.observe(viewLifecycleOwner) {
            viewModel.reloadListProductData()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun init() {
        productData = arguments?.get("product_data") as ProductData
        product = productData.product
        favorite = productData.favorite
    }


    override fun setViews() {

        with(binding) {

            productDescription.text = product.description
            productRatingBar.rating = product.reviewStars.toFloat()
            tvNumberReviews.text = product.numberReviews.toString()
            productBrand.text = product.brandName
            productPrice.text =
                binding.root.context.resources.getString(R.string.money_unit_float, product.getDiscountPrice())
            productTitle.text = product.title
            layoutRating.setOnClickListener {
                findNavController().navigate(R.id.action_go_review, bundleOf(Constants.KEY_PRODUCT to productData))
            }
        }

        /* drop down menu*/
        val adapterSize = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listSize)
        val adapterColor =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, product.getListColor())
        (binding.menuSize.editText as? AutoCompleteTextView)?.setAdapter(adapterSize)
        (binding.menuColor.editText as? AutoCompleteTextView)?.setAdapter(adapterColor)

        if (favorite != null) {
            binding.tvColor.setText(favorite!!.color, false)
            binding.tvSize.setText(favorite!!.size, false)
        }

        /* viewpager*/
        binding.viewPager.adapter = ImageProductDetailAdapter(product.images)
        binding.viewPager.autoScroll(3500)
        /* recyclerView*/
        relateProductAdapter = HomeProductListAdapter(this)
        val relateProducts = getRelateProducts()
        relateProductAdapter.setData(relateProducts, null)
        binding.rcvProducts.adapter = relateProductAdapter
        binding.rcvProducts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.tvNumItem.text =
            resources.getQuantityString(R.plurals.num_item, relateProducts.size, relateProducts.size)

        binding.btnAddToCart.setOnClickListener {
            val size = binding.menuSize.editText?.text.toString()
            val color = binding.menuColor.editText?.text.toString()
            if (color.isNullOrBlank()) {
                showToast(getString(R.string.please_select_color))
                binding.menuColor.requestFocus()
            } else {
                toggleBottomSheetInsertCart(
                    product, Cart(product.id, size, color, 1)
                )
            }
        }
        /* insert favorite btn */
        binding.ivFavorite.setOnClickListener {
            if (favorite == null) {
                toggleBottomSheetInsertFavorite(product)
            } else {
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
                Log.d(TAG, "setAppbar: ${it.itemId == R.drawable.ic_share} ")
                false
            }
            title = product.categoryName
        }
    }


    private fun onMenuClick(menuItem: MenuItem?): Boolean {
        Log.d(TAG, "onMenuClick: ${menuItem?.itemId}")
        when (menuItem?.itemId) {
            R.id.ic_search -> {
                Log.d(TAG, "onMenuClick: search clicked")
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

//    override fun onClickFavorite(product: Product, favorite: Favorite?) {
//        Log.d(TAG, "onClickFavorite: $favorite")
//        if (favorite == null) {
//            Log.d(TAG, "onClickFavorite: insert favorite")
//            toggleBottomSheetInsertFavorite(product)
//        } else {
//            Log.d(TAG, "onClickFavorite: remove favorite")
//            viewModel.removeFavorite(favorite!!)
//        }
//    }
//
//    override fun onClickItem(productData: ProductData) {
//        findNavController().navigate(
//            R.id.detail_dest,
//            bundleOf("product_data" to productData)
//        )
//    }
}


