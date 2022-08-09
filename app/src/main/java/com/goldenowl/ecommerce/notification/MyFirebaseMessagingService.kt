package com.goldenowl.ecommerce.notification

import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.goldenowl.ecommerce.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            remoteMessage.data.let {
                notifyCloudMessage(it["title"], it["body"], null)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            notifyCloudMessage(it.title, it.body, it.imageUrl)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }


    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    private fun notifyCloudMessage(title: String?, body: String?, imageUrl: Uri?) {
        val notificationId = 201
        var notificationBuilder = NotificationCompat.Builder(this, getString(R.string.channel_cloud_message_id))
            .setSmallIcon(R.mipmap.ic_ecommerce_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        imageUrl?.let {
            val futureTarget = Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .submit()
            val bitmap = futureTarget.get()
            notificationBuilder.setLargeIcon(bitmap)
        }
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notificationBuilder.build())
        }
    }
}