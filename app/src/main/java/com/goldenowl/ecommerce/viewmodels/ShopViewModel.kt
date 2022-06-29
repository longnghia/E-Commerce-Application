package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    private val productsRepository = (application as MyApplication).productsRepository
    private val settingsManager: SettingsManager = SettingsManager(application as MyApplication)

    var mListProduct: List<Product> = ArrayList()

    var categoryList: Set<String> = setOf()

    var listProductData: MutableLiveData<List<ProductData>> = MutableLiveData<List<ProductData>>()
    var listReviewData: MutableLiveData<MutableList<ReviewData>> = MutableLiveData<MutableList<ReviewData>>()
    var listPromo: MutableLiveData<List<Promo>> = MutableLiveData<List<Promo>>().apply { value = emptyList() }
    var listCard: MutableLiveData<List<Card>> = MutableLiveData<List<Card>>().apply { value = emptyList() }

    var curBag: MutableLiveData<Bag> = MutableLiveData<Bag>()
    var orderPrice: MutableLiveData<Float> = MutableLiveData<Float>()
    var defaultCardIndex: MutableLiveData<Int?> = MutableLiveData<Int?>()
    var defaultAddressIndex: MutableLiveData<Int?> = MutableLiveData<Int?>()
    var deliveryMethod: MutableLiveData<Delivery?> = MutableLiveData<Delivery?>()

    val toastMessage: MutableLiveData<String?> = MutableLiveData<String?>()

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

    private var isNetworkAvailable = false


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

        defaultAddressIndex.value = settingsManager.getDefaultAddress()
        defaultCardIndex.value = settingsManager.getDefaultCard()
    }

    private fun getUser() = authRepository.getUser()
    fun isLoggedIn() = authRepository.isUserLoggedIn()

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

        Log.d(
            TAG,
            "fetchData: \n$mListProduct \nresCard=$resCard \nresCard=$resAddress \nresCard=$defaultCheckOut"
        )

        if (resCard is MyResult.Success) {
            listCard.value = resCard.data
        }


        //todo restore user data
//        if (defaultCheckOut is MyResult.Success) {
//            val data = defaultCheckOut.data
//            val cardIndex = data.getOrDefault(Constants.DEFAULT_CARD, -1)
//            defaultCardIndex.value = cardIndex
//
//            val addressIndex = data.getOrDefault(Constants.DEFAULT_ADDRESS, -1)
//            defaultAddressIndex.value = addressIndex
//        }
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
                Log.d(TAG, "emptyCartTable: $it")
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
                    val newList = listCard.value?.toMutableList() ?: mutableListOf()
                    newList.add(card)
                    listCard.postValue(newList)
                    setToast("Add card successfully!")

                } else if (it is MyResult.Error) {
                    toastMessage.postValue(it.exception.message)
                }
            }
        }
    }

    fun setDefaultCard(default: Int) {
        setDefaultCheckOut(
            mapOf(
                Constants.DEFAULT_CARD to default,
                Constants.DEFAULT_ADDRESS to defaultAddressIndex.value
            )
        )
        defaultCardIndex.value = default
    }

    fun setDefaultAddress(default: Int) {
        setDefaultCheckOut(
            mapOf(
                Constants.DEFAULT_CARD to defaultCardIndex.value,
                Constants.DEFAULT_ADDRESS to default
            )
        )
        defaultAddressIndex.value = default
    }

    private fun setDefaultCheckOut(default: Map<String, Int?>) {
        settingsManager.setDefaultCheckOut(default)
        viewModelScope.launch(Dispatchers.IO) {
            productsRepository.setDefaultCheckOut(default).let {
                Log.d(TAG, "setDefaultCheckOut: result= $it")
                if (it is MyResult.Success) {
                } else if (it is MyResult.Error) {
                    setToast(it.exception.message)
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
                    listCard.postValue(list ?: emptyList())
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
            setDefaultAddress(0)
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

    private fun setToast(msg: String?) {
        toastMessage.postValue(msg)
        toastMessage.postValue(null)
    }

    private fun showToast(msg: String?) {
        if (msg.isNullOrBlank())
            return
        Toast.makeText((getApplication() as MyApplication).applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getString(resId: Int): String {
        return (getApplication() as MyApplication).applicationContext.getString(resId)
    }

    fun getUserId(): String {
        return authRepository.getUserId()
    }

    fun getReviewByProductId(productId: String) {
        loadingStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch {
            var helpfulList: List<String> = emptyList()
            if (isLoggedIn()) {
                val helpfulJob = async { productsRepository.getHelpfulReview() }
                val helpfulRes = helpfulJob.await()
                Log.d(TAG, "getReviewByProductId: helpfulRes=$helpfulRes")
                if (helpfulRes is MyResult.Error) {
                    showToast(helpfulRes.exception.message)
                } else if (helpfulRes is MyResult.Success) {
                    helpfulList = helpfulRes.data
                }
            }
            val reviewJob = async { productsRepository.getReviewByProductId(productId) }
            val reviewRes = reviewJob.await()

            if (reviewRes is MyResult.Error) {
                showToast(reviewRes.exception.message)
                loadingStatus.value = BaseLoadingStatus.FAILED
            } else if (reviewRes is MyResult.Success) {
                val listReview = reviewRes.data
                getListReviewData(listReview, helpfulList)
                loadingStatus.value = BaseLoadingStatus.SUCCEEDED
            }
        }
    }

    fun sendReview(
        userId: String,
        productId: String,
        rating: Int,
        review: String,
        listImages: MutableList<String>,
        date: Date
    ) {
        loadingStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch {
            val uploadResult = productsRepository.uploadReviewImages(listImages)
            Log.d(TAG, "sendReview: uploadImages: $uploadResult")
            if (uploadResult is MyResult.Error) {
                setToast(uploadResult.exception.message)
            } else if (uploadResult is MyResult.Success) {
                val listUrl = uploadResult.data
                val review = Review(userId, productId, rating, review, listUrl, date)
                val result = productsRepository.sendReview(review)
                Log.d(TAG, "sendReview: $result")
                if (result is MyResult.Success) {
                    val newList = listReviewData.value?.toMutableList() ?: mutableListOf()
                    val reviewData = ReviewData(result.data, review, getUser(), false)
                    newList.add(0, reviewData)
                    listReviewData.value = newList
                    loadingStatus.value = BaseLoadingStatus.SUCCEEDED
                    updateProductReviewInfo(productId, newList)
                } else if (result is MyResult.Error) {
                    setToast(result.exception.message)
                }
            }
        }
    }

    private fun updateProductReviewInfo(productId: String, listReviewData: MutableList<ReviewData>) {
        val product = mListProduct.find { it.id == productId } ?: return
        product.numberReviews = listReviewData.size
        product.reviewStars = ReviewData.getAverageRating(listReviewData).toInt()
        viewModelScope.launch {
            val res = productsRepository.updateProduct(product)
            Log.d(TAG, "updateProductReviewInfo: $res")
            if (res is MyResult.Error) {
                showToast(res.exception.message)
            }
        }
    }

    private suspend fun getListReviewData(listReview: List<ReviewData>, listHelpful: List<String>) {
        var mUser: User? = null
        val listData = listReview.map { reviewData ->
            val review = reviewData.review
            val userRes = authRepository.getUserById(review.userId)
            if (userRes is MyResult.Success) {
                mUser = userRes.data
            } else if (userRes is MyResult.Error) {
                setToast(userRes.exception.message)
            }
            reviewData.user = mUser
            reviewData.helpful = listHelpful.contains(reviewData.reviewId)
            reviewData
        }
        Log.d(TAG, "getListReviewData:\n$listHelpful\n$listData")
        listReviewData.value = listData.toMutableList()
    }

    fun updateHelpful(reviewData: ReviewData) {
        viewModelScope.launch {
            val res = productsRepository.updateHelpful(reviewData.reviewId, reviewData.helpful)
            Log.d(TAG, "updateHelpful: $res")
            if (res is MyResult.Error) {
                showToast(res.exception.message)
            }
        }
    }

    companion object {
        val TAG = "ProductViewModel"
    }
}
