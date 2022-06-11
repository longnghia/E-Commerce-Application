package com.goldenowl.ecommerce.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.utils.SortType

//class SortFilterViewModel(application: Application) : AndroidViewModel(application) {
//class SortFilterViewModel() : ViewModel() {
class SortFilterViewModel {

    var filterList: Set<String> = setOf()
    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = SortType.PRICE_INCREASE }
    val filterType: MutableLiveData<String?> = MutableLiveData<String?>()
        .apply { value = null }
    val searchTerm: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    private fun setCategoryList(list: List<Product>) {
        val categoryList = mutableSetOf<String>()
        if (list.isNotEmpty()) {
            for (p in list) {
                categoryList.add(p.categoryName)
            }
        }
        Log.d(TAG, "getCategoryList: $categoryList")
        this.filterList = categoryList
    }

    fun setSortType(type: SortType) {
        sortType.value = type
    }

    fun setFilterType(type: String) {
        filterType.value = type
    }


//    fun searchProducts(query: String) {
//        if (query.isEmpty())
//            filterProducts.value = mListProduct.toList()
//        var currentFilterList = mListProduct.filter {
//            it.title.indexOf(query, ignoreCase = true) >= 0 || it.brandName.indexOf(query, ignoreCase = true) >= 0
//        }
//        filterProducts.value = currentFilterList
//    }


    companion object {
        val TAG = "SortFilterViewmodel"
    }
}





