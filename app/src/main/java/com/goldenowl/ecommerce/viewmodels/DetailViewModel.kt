package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.models.data.Product
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    val seller = MutableLiveData<User>()

    fun initData(product: Product) {
        viewModelScope.launch {
            val sellerRes = authRepository.getUserById(product.sellerId)
            if (sellerRes is MyResult.Success)
                seller.postValue(sellerRes.data)
        }
    }
}
