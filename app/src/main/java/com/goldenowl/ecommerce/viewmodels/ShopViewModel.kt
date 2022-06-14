package com.goldenowl.ecommerce.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.goldenowl.ecommerce.models.data.Cart
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.models.repo.AuthRepository
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShopViewModel(private val productsRepository: ProductsRepository, private val authRepository: AuthRepository) :
    ViewModel() {

    var mListProduct: List<Product> = ArrayList()
    private val userId: String? = if (authRepository.isUserLoggedIn()) authRepository.getUserId() else null

    var categoryList: Set<String> = setOf()

    var listProductData: MutableLiveData<List<ProductData>> = MutableLiveData<List<ProductData>>()

    val toastMessage: MutableLiveData<String> = MutableLiveData<String>()

    val dataReady: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.LOADING
    }

    val allFavorite = productsRepository.allFavorite.asLiveData()
    val allCart = productsRepository.allCart.asLiveData()

    init {
        viewModelScope.launch {
            getAllProducts()
        }
    }

    fun reloadListProductData() {
        Log.d(TAG, "reloadListProductData: \nallFavorite=${allFavorite.value}\nallCart=${allCart.value}")
        listProductData.value = mListProduct.map { p ->
            val favorite: Favorite? = allFavorite.value?.find {
                it.productId == p.id
            }
            val cart: Cart? = allCart.value?.find {
                it.productId == p.id
            }
            ProductData(p, favorite, cart)
        }
    }

    private fun setCategoryList(list: List<Product>) {
        val categoryList = mutableSetOf<String>()
        if (list.isNotEmpty()) {
            for (p in list) {
                categoryList.add(p.categoryName)
            }
        }
        Log.d(TAG, "getCategoryList: $categoryList")
        this.categoryList = categoryList
    }

    private suspend fun getAllProducts() {

        Log.d(TAG, "getAllProducts: start")
        if (mListProduct.isEmpty()) {
            mListProduct = productsRepository.getAllProducts()
            Log.d(TAG, "getAllProducts: done = $mListProduct")
//            productsRepository.syncLocalDataSource(mProductsList) // todo
        } else {
            Log.d(TAG, "getAllProducts: using data in mProductList:\n $mListProduct")
        }
        setCategoryList(mListProduct)
//        filterProducts.value = getFilterProducts(-1, mListProduct)
//        filterFavoriteProducts.value = getFilterProducts(-1, mListProduct)
        dataReady.value = BaseLoadingStatus.SUCCEEDED
    }

    private fun getFilterProducts(index: Int, fromList: List<Product>): List<Product> {
        Log.d(TAG, "getFilterProducts: position $index")
        var filteredProducts = fromList.toList()
        if (index < 0) {
            return filteredProducts
        }

        filteredProducts = filteredProducts.filter {
            it.categoryName == categoryList.elementAt(index)
        }
        return filteredProducts
    }

    fun insertFavorite(favorite: Favorite) {
        Log.d(TAG, "insertFavorite: $favorite")
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertFavorite(favorite).let {
                Log.d(TAG, "insertFavorite: result= $it")
                if (it is MyResult.Success) {

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeFavorite(favorite).let {
                if (it is MyResult.Success) {
                    Log.d(TAG, "removeFavorite: success= $it")

                } else if (it is MyResult.Error) {
                    Log.e(TAG, "removeFavorite: ERROR ", it.exception)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun insertCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertCart(cart).let {
                Log.d(TAG, "insertCart: result= $it")
                if (it is MyResult.Success) {

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeCart(cart).let {
                if (it is MyResult.Success) {
                    Log.d(TAG, "removeCart: success= $it")
                } else if (it is MyResult.Error) {
                    Log.e(TAG, "removeFavorite: ERROR ", it.exception)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun getRelateProducts(tags: List<Product.Tag>): List<ProductData> {
        val list = listProductData.value?.toMutableList()
        return list?.take(5) ?: emptyList()
        //todo getRelateProducts
//        list.filter { product->
//            product.tags.find { tag->
//                tags.indexOf(tag.)
//            }
//        }
    }

//    fun restoreUserDatabase() {
//        viewModelScope.launch {
//            val userOrderResult = productsRepository.getUserOrder(userId!!)
//            Log.d(TAG, "restoreUserDatabase: $userOrderResult")
//            if (userOrderResult is MyResult.Success) {
//                val remoteDatabase = userOrderResult.data
//                productsRepository.updateLocalDatabase(remoteDatabase)
//            } else if (userOrderResult is MyResult.Error) {
//                toastMessage.value = userOrderResult.exception.message
//            }
//
//        }
//    }

    companion object {
        val TAG = "ProductViewModel"
    }
}

class ProductViewModelFactory(
    private val productsRepository: ProductsRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopViewModel(productsRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

