package com.goldenowl.ecommerce.ui.global.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.models.data.Favorite
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.ProductData
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    var mListFavorite: List<Favorite>? = null
    private val productsRepository = (application as MyApplication).productsRepository
    val loading: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.NONE
    }
    val endList: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = false
    }
    val toastMessage: MutableLiveData<String> = MutableLiveData<String>()

    var listProductData: MutableLiveData<List<ProductData>> = MutableLiveData<List<ProductData>>()
    private var listProduct: MutableList<Product> = mutableListOf()

    fun loadFirstPage(category: String?) {
        loading.value = BaseLoadingStatus.LOADING
        endList.value = false
        viewModelScope.launch {
            val loadRes = productsRepository.loadFirstPage(category)
            Log.d(TAG, "loadFirstPage: $loadRes")
            if (loadRes is MyResult.Success) {
                loading.value = BaseLoadingStatus.SUCCEEDED
                val list = loadRes.data
                listProduct = list
                loadListProductData(mListFavorite)
            } else if (loadRes is MyResult.Error) {
                loading.value = BaseLoadingStatus.FAILED
                toastMessage.postValue(loadRes.exception.message)
            }
        }
    }

    fun loadMorePage(category: String?) {
        loading.value = BaseLoadingStatus.LOADING
        viewModelScope.launch {
            val loadRes = productsRepository.loadMorePage(category)
            Log.d(TAG, "loadMorePage: $loadRes")
            if (loadRes is MyResult.Success) {
                loading.value = BaseLoadingStatus.SUCCEEDED
                val list = loadRes.data
                if (list.isEmpty()) {
                    endList.value = true
                    return@launch
                }
                listProduct.addAll(list)
                loadListProductData(mListFavorite)
            } else if (loadRes is MyResult.Error) {
                loading.value = BaseLoadingStatus.FAILED
                toastMessage.postValue(loadRes.exception.message)
            }
        }
    }

    fun loadListProductData(listFavorite: List<Favorite>?) {
        if (listProduct.isEmpty()) {
            return
        }
        listProductData.value = listProduct.map {
            val favorite = listFavorite?.find { favorite ->
                favorite.productId == it.id
            }
            ProductData(it, favorite, null)
        }
    }

    companion object {
        val TAG = "CategoryViewModel"
    }
}