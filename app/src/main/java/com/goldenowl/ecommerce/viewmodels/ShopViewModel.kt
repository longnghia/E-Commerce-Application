package com.goldenowl.ecommerce.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.UserOrder
import com.goldenowl.ecommerce.models.repo.AuthRepository
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShopViewModel(private val productsRepository: ProductsRepository, private val authRepository: AuthRepository) :
    ViewModel() {

    var mProductsList: List<Product> = ArrayList()


    private val userId: String? = if (authRepository.isUserLoggedIn()) authRepository.getUserId() else null

    var categoryList: Set<String> = setOf()

    val productsList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = SortType.PRICE_INCREASE }

    val currentCategory: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = -1 }

    val filterProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
    val filterFavoriteProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

    val favoriteList: MutableLiveData<List<UserOrder.Favorite>> = MutableLiveData<List<UserOrder.Favorite>>()
    val bagList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

    val dataReady: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.LOADING
    }

    init {
        viewModelScope.launch {
            getAllProducts()
            getFavoriteList()
        }
    }

    private suspend fun getFavoriteList(): List<UserOrder.Favorite> {
        val favoriteList = productsRepository.getFavoriteList(userId)
        Log.d(TAG, "getFavoriteList: $favoriteList")
        val favoriteProducts = favoriteList.map { it.productId }
        filterFavoriteProducts.value = mProductsList.toMutableList().filter { favoriteProducts.indexOf(it.id) >= 0 }
        return favoriteList
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
        if (mProductsList.isEmpty()) {
            mProductsList = productsRepository.getAllProducts()
            Log.d(TAG, "getAllProducts: done = $mProductsList")
//            productsRepository.syncLocalDataSource(mProductsList) // todo
            productsList.value = mProductsList.toList()
        } else {
            Log.d(TAG, "getAllProducts: using data in mProductList:\n $mProductsList")
        }
        setCategoryList(mProductsList)
        filterProducts.value = getFilterProducts(-1, mProductsList)
        filterFavoriteProducts.value = getFilterProducts(-1, mProductsList)
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

    fun setSortBy(type: SortType) {
        sortType.value = type
    }

    fun sortBy(type: SortType) {
        Log.d(TAG, "sortBy: $type")
        val currentFilterList = filterProducts.value
        if (currentFilterList != null) {
            var resultList = when (type) {
                SortType.REVIEW -> currentFilterList.sortedByDescending { it.reviewStars }
                SortType.PRICE_DECREASE -> currentFilterList.sortedByDescending { it.getDiscountPrice() }
                SortType.PRICE_INCREASE -> currentFilterList.sortedBy { it.getDiscountPrice() }
                SortType.POPULAR -> currentFilterList.sortedByDescending { it.isPopular }
                SortType.NEWEST -> currentFilterList.sortedByDescending { it.createdDate }
            }
            filterProducts.value = resultList
        }
    }

    fun searchProducts(query: String) {
        if (query.isEmpty())
            filterProducts.value = mProductsList.toList()
        var currentFilterList = mProductsList.filter {
            it.title.indexOf(query, ignoreCase = true) >= 0 || it.brandName.indexOf(query, ignoreCase = true) >= 0
        }
        filterProducts.value = currentFilterList
    }

    fun addToFavorite(favorite: UserOrder.Favorite) {
        Log.d(TAG, "addToFavorite: userId = $userId")
        viewModelScope.launch(Dispatchers.IO) {
            if (userId != null) {
                productsRepository.addToFavorite(favorite, userId)
            }
        }
        favoriteList.value?.toMutableList().let {
            if (it != null) {
                it.add(favorite)
                Log.d(TAG, "addToFavorite: favorite change")
                favoriteList.value = it
            } else {
                Log.d(TAG, "addToFavorite: favorite list NULL")
            }
        }
    }

    fun createUserOrderTable() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                productsRepository.createUserOrderTable(userId!!)
            } else {
                Log.d(TAG, "createUserOrderTable: USER NOT LOG GIN")
            }
        }
    }

    fun setFilterProducts(it: Int?) {
        filterProducts.value = getFilterProducts(it ?: -1, mProductsList)
    }


    companion object {
        val TAG = "ProductViewModel"
    }
}

enum class SortType {
    POPULAR, NEWEST, REVIEW, PRICE_INCREASE, PRICE_DECREASE
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

