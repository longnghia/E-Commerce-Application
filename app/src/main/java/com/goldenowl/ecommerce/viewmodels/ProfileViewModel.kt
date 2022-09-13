package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    private val currentUserId: String? = try {
        authRepository.getUserId()
    } catch (e: Exception) {
        null
    }

    val user = MutableLiveData<User?>()

    init {
        viewModelScope.launch {
            currentUserId?.also {
                when (val res = authRepository.getUserById(it)) {
                    is MyResult.Success -> {
                        user.postValue(res.data)
                    }
                    else -> {}
                }
            }

        }
    }
}
