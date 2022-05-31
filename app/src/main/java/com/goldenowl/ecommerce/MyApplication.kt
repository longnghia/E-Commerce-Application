package com.goldenowl.ecommerce

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.goldenowl.ecommerce.models.data.LocalProductsDataSource
import com.goldenowl.ecommerce.models.data.RemoteProductsDataSource
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    lateinit var productsRepository : ProductsRepository
    private val TAG = "MyApplication"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: app on create")
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        FirebaseApp.initializeApp(this)

        productsRepository =ProductsRepository(RemoteProductsDataSource(), LocalProductsDataSource())
    }
}