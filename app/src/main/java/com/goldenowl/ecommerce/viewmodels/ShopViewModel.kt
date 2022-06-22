package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.models.data.*
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants
import com.goldenowl.ecommerce.utils.MyResult
import com.goldenowl.ecommerce.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    private val productsRepository = (application as MyApplication).productsRepository
    private val settingsManager: SettingsManager = SettingsManager(application as MyApplication)

    var mListProduct: List<Product> = ArrayList()

    private val userLoggedIn: Boolean = authRepository.isUserLoggedIn()

    var categoryList: Set<String> = setOf()

    var listProductData: MutableLiveData<List<ProductData>> = MutableLiveData<List<ProductData>>()
    var listPromo: MutableLiveData<List<Promo>> = MutableLiveData<List<Promo>>().apply { value = emptyList() }
    var listCard: MutableLiveData<List<Card>> = MutableLiveData<List<Card>>().apply { value = emptyList() }

    var curBag: MutableLiveData<Bag> = MutableLiveData<Bag>()
    var orderPrice: MutableLiveData<Float> = MutableLiveData<Float>()
    var defaultCardIndex: MutableLiveData<Int?> = MutableLiveData<Int?>()
    var defaultCard: MutableLiveData<Card?> = MutableLiveData<Card?>()
    var defaultAddressIndex: MutableLiveData<Int?> = MutableLiveData<Int?>()
    var defaultAddress: MutableLiveData<Address?> = MutableLiveData<Address?>()
    var deliveryMethod: MutableLiveData<Delivery?> = MutableLiveData<Delivery?>()

    val toastMessage: MutableLiveData<String> = MutableLiveData<String>()

    val dataReady: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.LOADING
    }

    val allFavorite = productsRepository.allFavorite.asLiveData()
    val allCart = productsRepository.allCart.asLiveData()
    val allAddress = productsRepository.allAddress.asLiveData()

    var addAddressStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.NONE
    }
    var loadingStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>().apply {
        value = BaseLoadingStatus.NONE
    }

    var isNetworkAvailable = false


    init {
        isNetworkAvailable = Utils.isNetworkAvailable(application.applicationContext)
        productsRepository.setNetworkAvailable(isNetworkAvailable)
        settingsManager.setLastNetwork(isNetworkAvailable)

        viewModelScope.launch {
            if (!settingsManager.getLastNetwork() && isNetworkAvailable) {
                local2remote()
            }
            fetchData()
            getListPromo()
            dataReady.value = BaseLoadingStatus.SUCCEEDED
        }

        curBag.value = Bag()
    }

    private suspend fun local2remote() {
        productsRepository.local2remote()
    }

    fun reloadListProductData() {
        listProductData.value = mListProduct.map { p ->
            val favorite: Favorite? = allFavorite.value?.find {
                it.productId == p.id
            }
            val cart: Cart? = allCart.value?.find {
                it.productId == p.id
            }
            ProductData(p, favorite, cart)
        }
    }

    private fun setCategoryList(list: List<Product>) {
        val categoryList = mutableSetOf<String>()
        if (list.isNotEmpty()) {
            for (p in list) {
                categoryList.add(p.categoryName)
            }
        }
        this.categoryList = categoryList
    }

    private suspend fun fetchData() {
        mListProduct = productsRepository.getAllProducts()
        setCategoryList(mListProduct)

        val resCard = productsRepository.getListCard()
        val resAddress = productsRepository.getListAddress()
        val defaultCheckOut = productsRepository.getDefaultCheckOut()

        Log.d(TAG, "fetchData: \n$mListProduct \n$resCard, \n$resAddress \n$defaultCheckOut")

        if (resCard is MyResult.Success) {
            listCard.value = resCard.data
        }
        if (defaultCheckOut is MyResult.Success) {
            val data = defaultCheckOut.data
            val cardIndex = data.getOrDefault(Constants.DEFAULT_CARD, -1)
            defaultCardIndex.value = cardIndex
            defaultCard.value = listCard.value?.get(cardIndex)

            val addressIndex = data.getOrDefault(Constants.DEFAULT_ADDRESS, -1)
            defaultAddressIndex.value = addressIndex
            defaultAddress.value = allAddress.value?.get(addressIndex)
            Log.d(TAG, "fetchData: defaultAddress=${defaultAddress.value}")
        }
    }

    private suspend fun getListPromo() {
        val res = productsRepository.getListPromo()
        Log.d(TAG, "getListPromo: $res")
        if (res is MyResult.Error) {
            toastMessage.value = res.exception.message
        } else if (res is MyResult.Success) {
            listPromo.value = res.data
        }
    }

    fun insertFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertFavorite(favorite).let {
                Log.d(TAG, "insertFavorite: result= $it")
                if (it is MyResult.Success) {

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeFavorite(favorite).let {
                if (it is MyResult.Success) {
                    Log.d(TAG, "removeFavorite: success= $it")

                } else if (it is MyResult.Error) {
                    Log.e(TAG, "removeFavorite: ERROR ", it.exception)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun insertCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertCart(cart).let {
                Log.d(TAG, "insertCart: result= $it")
                if (it is MyResult.Success) {

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeCart(cart).let {
                if (it is MyResult.Success) {
                    Log.d(TAG, "removeCart: success= $it")
                } else if (it is MyResult.Error) {
                    Log.e(TAG, "removeFavorite: ERROR ", it.exception)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun emptyCartTable() {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.emptyCartTable().let {
                Log.e(TAG, "emptyCartTable: $it")
                if (it is MyResult.Success) {
                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun updateCart(cart: Cart, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.updateCart(cart, position).let {
                Log.d(TAG, "updateCart: result= $it")
                if (it is MyResult.Success) {

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }


    fun insertOrder(order: Order) {
        loadingStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertOrder(order).let {
                Log.d(TAG, "insertOrder: result= $it")
                if (it is MyResult.Success) {
                    loadingStatus.postValue(BaseLoadingStatus.SUCCEEDED)
                } else if (it is MyResult.Error) {
                    loadingStatus.postValue(BaseLoadingStatus.FAILED)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeOrder(order).let {
                if (it is MyResult.Success) {
                    Log.d(TAG, "removeOrder: success= $it")
                } else if (it is MyResult.Error) {
                    Log.e(TAG, "removeFavorite: ERROR ", it.exception)
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }


    fun getRelateProducts(tags: List<Product.Tag>): List<ProductData> {
        val list = listProductData.value?.toMutableList()
        return list?.take(5) ?: emptyList()
        //todo getRelateProducts
//        list.filter { product->
//            product.tags.find { tag->
//                tags.indexOf(tag.)
//            }
//        }
    }


    fun insertCard(card: Card) {
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertCard(card).let {
                Log.d(TAG, "insertCard: result= $it")
                if (it is MyResult.Success) {
                    val newList = listCard.value?.toMutableList()
                    newList?.add(card)
                    listCard.postValue(newList)
                    toastMessage.value = "Add card successfully!"

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun setDefaultCard(default: Int, card: Card) {
        setDefaultCheckOut(
            mapOf(
                Constants.DEFAULT_CARD to default,
                Constants.DEFAULT_ADDRESS to defaultAddressIndex.value
            )
        )
        defaultCardIndex.value = default
        defaultCard.value = card
    }

    fun setDefaultAddress(default: Int, address: Address?) {
        setDefaultCheckOut(
            mapOf(
                Constants.DEFAULT_CARD to defaultCardIndex.value,
                Constants.DEFAULT_ADDRESS to default
            )
        )
        defaultAddressIndex.value = default
        defaultAddress.value = address
    }

    private fun setDefaultCheckOut(default: Map<String, Int?>) {
        settingsManager.setDefaultCheckOut(default)
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.setDefaultCheckOut(default).let {
                Log.d(TAG, "setDefaultCheckOut: result= $it")
                if (it is MyResult.Success) {
                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeCard(position: Int) {
        if (position == defaultCardIndex.value) {
            defaultCardIndex.value = 0
        }
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.removeCard(position).let {
                Log.d(TAG, "removeCard: result= $it")
                if (it is MyResult.Success) {
                    val list = listCard.value?.toMutableList()
                    list?.removeAt(position)
                    listCard.postValue(list)
                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun insertAddress(address: Address) {
        addAddressStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.insertAddress(address).let {
                Log.d(TAG, "insertAddress: result= $it")
                if (it is MyResult.Success) {
                    addAddressStatus.postValue(BaseLoadingStatus.SUCCEEDED)
                } else if (it is MyResult.Error) {
                    addAddressStatus.value = BaseLoadingStatus.FAILED
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun removeAddress(position: Int) {
        addAddressStatus.value = BaseLoadingStatus.LOADING
        if (position == defaultAddressIndex.value) {
            setDefaultAddress(0, allAddress.value?.getOrNull(0))
        }
        val address = this.allAddress.value?.get(position)
        viewModelScope.launch(Dispatchers.IO) {
            if (address != null) {
                productsRepository.removeAddress(position, address).let {
                    Log.d(TAG, "removeAddress: result= $it")
                    if (it is MyResult.Success) {
                        addAddressStatus.value = BaseLoadingStatus.SUCCEEDED
                    } else if (it is MyResult.Error) {
                        toastMessage.postValue(it.exception.message)
                        addAddressStatus.value = BaseLoadingStatus.FAILED
                    }
                }
            }
        }
    }

    fun updateAddress(address: Address, position: Int) {
        addAddressStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.updateAddress(address, position).let {
                Log.d(TAG, "updateAddress: result= $it")
                if (it is MyResult.Success) {
                    addAddressStatus.postValue(BaseLoadingStatus.SUCCEEDED)
                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                    addAddressStatus.value = BaseLoadingStatus.FAILED
                }
            }
        }
    }


//    fun restoreUserDatabase() {
//        if(userLoggedIn == null)
//            return
//        viewModelScope.launch {
//            val userOrderResult = productsRepository.getUserOrder(userLoggedIn)
//            Log.d(TAG, "restoreUserDatabase: $userOrderResult")
//            if (userOrderResult is MyResult.Success) {
//                val remoteDatabase = userOrderResult.data
//                productsRepository.updateLocalDatabase(remoteDatabase)
//            } else if (userOrderResult is MyResult.Error) {
//                toastMessage.value = userOrderResult.exception.message
//            }
//
//        }
//    }


    companion object {
        val TAG = "ProductViewModel"
    }
}
