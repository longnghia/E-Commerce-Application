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

    val productsList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
    var listProductData: MutableLiveData<List<ProductData>> = MutableLiveData<List<ProductData>>()

//    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
//        .apply { value = SortType.PRICE_INCREASE }

//    val filterProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
//    val filterFavoriteProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()


//    val favoriteList: MutableLiveData<List<Favorite>> = MutableLiveData<List<Favorite>>()
//    val bagList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

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
        Log.d(TAG, "reloadListProductData: allFavorite=${allFavorite.value}")
        listProductData.value = mListProduct.map { p ->
            val favorite: Favorite? = allFavorite.value?.find {
                it.productId == p.id
            }
            val cart: Cart? = null // todo allCart.value
            ProductData(p, favorite, cart)
        }
    }

//    private fun getFavoriteList(): List<Favorite> {
//        val favoriteList = productsRepository.observeListFavorite()
//
//        Log.d(TAG, "getFavoriteList: $favoriteList")
//        val favoriteProducts = favoriteList.map { it.productId }
//        filterFavoriteProducts.value = mProductsList.toMutableList().filter { favoriteProducts.indexOf(it.id) >= 0 }
//        return favoriteList
//    }

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
            productsList.value = mListProduct.toList()
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

//    fun sortBy(type: SortType) {
//        Log.d(TAG, "sortBy: $type")
////        val currentFilterList = filterProducts.value
//        if (currentFilterList != null) {
//            var resultList = when (type) {
//                SortType.REVIEW -> currentFilterList.sortedByDescending { it.reviewStars }
//                SortType.PRICE_DECREASE -> currentFilterList.sortedByDescending { it.getDiscountPrice() }
//                SortType.PRICE_INCREASE -> currentFilterList.sortedBy { it.getDiscountPrice() }
//                SortType.POPULAR -> currentFilterList.sortedByDescending { it.isPopular }
//                SortType.NEWEST -> currentFilterList.sortedByDescending { it.createdDate }
//            }
//            filterProducts.value = resultList
//        }
//    }

//    fun searchProducts(query: String) {
//        if (query.isEmpty())
//            filterProducts.value = mListProduct.toList()
//        var currentFilterList = mListProduct.filter {
//            it.title.indexOf(query, ignoreCase = true) >= 0 || it.brandName.indexOf(query, ignoreCase = true) >= 0
//        }
//        filterProducts.value = currentFilterList
//    }

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

//    fun setFilterProducts(it: Int?) {
//        filterProducts.value = getFilterProducts(it ?: -1, mListProduct)
//    }

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
                Log.d(TAG, "insertFavorite: result= $it")
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

