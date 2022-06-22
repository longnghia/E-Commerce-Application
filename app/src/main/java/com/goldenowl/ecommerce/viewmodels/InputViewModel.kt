package com.goldenowl.ecommerce.viewmodels

import androidx.lifecycle.MutableLiveData
import com.goldenowl.ecommerce.utils.TextValidation

class InputViewModel {

    var errorCardName: MutableLiveData<String> = MutableLiveData<String>()
    var errorCardNumber: MutableLiveData<String> = MutableLiveData<String>()
    var errorCardExpireDate: MutableLiveData<String> = MutableLiveData<String>()
    var errorCardCVV: MutableLiveData<String> = MutableLiveData<String>()
    var newCardValid: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        newCardValid.value = false
    }

    fun setNewCardValid() {
        newCardValid.value = errorCardName.value.isNullOrEmpty() &&
                errorCardNumber.value.isNullOrEmpty() &&
                errorCardExpireDate.value.isNullOrEmpty() &&
                errorCardCVV.value.isNullOrEmpty()
    }

    fun checkCardName(name: String) {
        val error = TextValidation.validateCardName(name)
        errorCardName.value = error
        setNewCardValid()
    }

    fun checkCardNumber(cardNumber: String) {
        val error = TextValidation.validateCardNumber(cardNumber)
        errorCardNumber.value = error
        setNewCardValid()
    }

    fun checkCardExpireDate(expireDate: String) {
        val error = TextValidation.validateCardExpireDate(expireDate)
        errorCardExpireDate.value = error
        setNewCardValid()
    }
    fun checkCardCvv(cvv: String) {
        val error = TextValidation.validateCardCvv(cvv)
        errorCardCVV.value = error
        setNewCardValid()
    }

    companion object {
        const val TAG = "TextInputViewModel"
    }
}