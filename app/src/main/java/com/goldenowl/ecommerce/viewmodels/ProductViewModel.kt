package com.goldenowl.ecommerce.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import kotlinx.coroutines.launch

class ProductViewModel(private val productsRepository: ProductsRepository) : ViewModel() {

    var categoryList: Set<String> = setOf()

    var mProductsList: List<Product> = ArrayList()
    val productsList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

    val testText: MutableLiveData<String> = MutableLiveData<String>()
    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = SortType.PRICE_INCREASE }

    val currentCategory: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = -1 }

    val filterProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
    val dataReady: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.LOADING
    }

    init {
        viewModelScope.launch {
            getAllProducts()
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

    suspend fun getAllProducts() {
        Log.d(TAG, "getAllProducts: start")
        if (mProductsList.isEmpty()) {
            mProductsList = productsRepository.getAllProducts()
            productsList.value = mProductsList.toList()
        } else {
            Log.d(TAG, "getAllProducts: using data in mProductList:\n $mProductsList")
        }
        setCategoryList(mProductsList)
        setFilterProducts(-1)
        dataReady.value = BaseLoadingStatus.SUCCESS
    }

    fun setFilterProducts(index: Int) {
        if (index < 0){
            filterProducts.value = mProductsList.toList()
            return
        }

        val filteredList = mProductsList.filter {
            it.categoryName == categoryList.elementAt(index)
        }
        filterProducts.value = filteredList
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


    companion object {
        val TAG = "ProductViewModel"
    }
}

enum class SortType {
    POPULAR, NEWEST, REVIEW, PRICE_INCREASE, PRICE_DECREASE
}

class ProductViewModelFactory(private val productsRepository: ProductsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(productsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

