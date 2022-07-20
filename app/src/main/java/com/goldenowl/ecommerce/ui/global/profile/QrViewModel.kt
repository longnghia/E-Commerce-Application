package com.goldenowl.ecommerce.ui.global.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QrViewModel(application: Application) : AndroidViewModel(application) {
    val timeOut = MutableLiveData<Boolean>()
    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    init {
        timeOut.value = false
    }

    fun setTimeout() {
        cancelTimeout()
        viewModelScope.launch(job) {
            delay(120000)
            timeOut.value = true
        }
    }

    fun cancelTimeout() {
        job.cancel()
        timeOut.value=false
    }
}