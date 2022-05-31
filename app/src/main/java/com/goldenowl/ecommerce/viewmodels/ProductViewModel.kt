package com.goldenowl.ecommerce.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import kotlinx.coroutines.launch

class ProductViewModel(private val productsRepository: ProductsRepository) : ViewModel() {

    lateinit var categoryList: Set<String>

    var mProductsList: List<Product> = ArrayList()
    val productsList: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

    val testText: MutableLiveData<String> = MutableLiveData<String>()
    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = SortType.PRICE_INCREASE }

    val currentCategory: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }

    val filterProducts: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()

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
        filterProducts.value = filterByCategory(0)
    }

    private fun filterByCategory(index: Int): List<Product>? {
        if (index < 0)
            return mProductsList

        val filteredList = mProductsList.filter {
            it.categoryName == categoryList.elementAt(index)
        }
        filterProducts.value = filteredList
        return filteredList
    }

    fun setSortBy(type: SortType) {
        sortType.value = type
    }

    fun sortBy(type: SortType) {
        Log.d(TAG, "sortBy: $type")
        var resultList = when (type) {
            SortType.REVIEW -> mProductsList.sortedBy { it.reviewStars }
            SortType.PRICE_DESCREASE -> mProductsList.sortedByDescending { it.getDiscountPrice() }
            SortType.PRICE_INCREASE -> mProductsList.sortedBy { it.getDiscountPrice() }
            SortType.POPULAR -> mProductsList.sortedByDescending { it.isPopular }
            SortType.NEWEST -> mProductsList.sortedByDescending { it.createdDate }
        }
        filterProducts.value = resultList
    }


    companion object {
        val TAG = "ProductViewModel"
    }
}

enum class SortType {
    POPULAR, NEWEST, REVIEW, PRICE_INCREASE, PRICE_DESCREASE
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

