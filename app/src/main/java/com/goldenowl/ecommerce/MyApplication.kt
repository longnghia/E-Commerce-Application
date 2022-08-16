package com.goldenowl.ecommerce

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.ECommerceDatabase
import com.goldenowl.ecommerce.models.repo.AuthRepository
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.goldenowl.ecommerce.models.repo.RemoteAuthDataSource
import com.goldenowl.ecommerce.models.repo.datasource.*
import com.goldenowl.ecommerce.utils.Constants
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    private val TAG = "MyApplication"
    val userManager by lazy { UserManager.getInstance(this) }
    private val database by lazy { ECommerceDatabase.getDatabase(this) }

    lateinit var productsRepository: ProductsRepository
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        FirebaseApp.initializeApp(this)

        createNotificationChannel()

        authRepository = AuthRepository(RemoteAuthDataSource(userManager, this), LocalAuthDataSource(userManager))

        val productDao = database.productDao()
        val favoriteDao = database.favoriteDao()
        val cartDao = database.cartDao()
        val orderDao = database.orderDao()
        val addressDao = database.addressDao()

        productsRepository = ProductsRepository(
            RemoteProductsDataSource(),
            RemoteReviewDataSource(userManager),
            RemoteFavoriteDataSource(userManager),
            RemoteOrderDataSource(userManager),
            RemoteAddressDataSource(userManager),
            RemotePaymentDataSource(userManager),
            RemoteCartDataSource(userManager),
            LocalProductsDataSource(productDao, favoriteDao, cartDao, orderDao, addressDao)
        )
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_change_password)
            val descriptionText = getString(R.string.channel_change_password_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_CHANGE_PASSWORD_ID, name, importance).apply {
                description = descriptionText
            }
            val channelCloud = NotificationChannel(
                getString(R.string.channel_cloud_message_id) ,
                getString(R.string.channel_cloud_message),
                importance).apply {
                description = getString(R.string.channel_cloud_message_description)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channelCloud)
        }
    }
}