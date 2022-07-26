package com.goldenowl.ecommerce.viewmodels

import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.utils.SortType

class SortFilterViewModel {

    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = null }
    val filterType: MutableLiveData<String?> = MutableLiveData<String?>()
        .apply { value = null }
    val filterByPrice: MutableLiveData<List<Float>?> = MutableLiveData<List<Float>?>()
        .apply { value = null }
    val searchTerm: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    fun disCardFilterPrice() {
        filterByPrice.value = null
    }

    companion object {
        val TAG = "SortFilterViewmodel"
    }
}





