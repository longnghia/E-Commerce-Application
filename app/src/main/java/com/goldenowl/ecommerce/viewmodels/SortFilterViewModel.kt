package com.goldenowl.ecommerce.viewmodels

import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.utils.SortType

//class SortFilterViewModel(application: Application) : AndroidViewModel(application) {
//class SortFilterViewModel() : ViewModel() {
class SortFilterViewModel {


    val sortType: MutableLiveData<SortType> = MutableLiveData<SortType>()
        .apply { value = SortType.PRICE_INCREASE }
    val filterType: MutableLiveData<String?> = MutableLiveData<String?>()
        .apply { value = null }
    val searchTerm: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    companion object {
        val TAG = "SortFilterViewmodel"
    }
}





