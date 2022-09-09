package services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ln.simplechat.R
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.utils.DateUtils


object ChatFMService {
    fun onMessageReceived(context: Context, remoteMessage: RemoteMessage, userId: String, appIconRes: Int) {
        var showNotification: Boolean
        remoteMessage.notification?.let {
            if (remoteMessage.data.isNotEmpty()) {
                remoteMessage.data.also { data ->
                    try {
                        val messageStr = data["message"]
                        val channelStr = data["channel"]
                        val senderStr = data["sender"]
                        val gson: Gson = GsonBuilder().create()
                        val typeMessage = object : TypeToken<Message>() {}.type
                        val typeChannel = object : TypeToken<Channel>() {}.type
                        val typeMember = object : TypeToken<Member>() {}.type
                        val channel: Channel =
                            gson.fromJson(channelStr, typeChannel)
                        val message: Message =
                            gson.fromJson(messageStr, typeMessage)
                        val sender: Member =
                            gson.fromJson(senderStr, typeMember)
                        Log.d(TAG, "onMessageReceived: \n$message \n$channel \n$sender")
                        showNotification = userId != message.sender
                        if (showNotification)
                            notifyCloudMessage(context, appIconRes, sender, channel, message)
                    } catch (e: Exception) {
                        Log.e(TAG, "onMessageReceived: ERROR", e)
                    }
                }
            } else {
                Log.w(TAG, "onMessageReceived: Cancel notification!")
            }
        }
    }

    val TAG = "CHAT_SERVICE"

    private fun notifyCloudMessage(
        context: Context,
        appIconRes: Int,
        sender: Member,
        channel: Channel,
        message: Message
    ) {
        val expandedView = RemoteViews(context.packageName, R.layout.notification_expanded)
        expandedView.setTextViewText(
            R.id.timestamp,
            DateUtils.SDF_HOUR.format(System.currentTimeMillis())
        )
        expandedView.setTextViewText(R.id.notification_message, message.text)
        expandedView.setImageViewBitmap(R.id.notification_img, getBitmap(context, sender.avatar))
        val collapsedView = RemoteViews(context.packageName, R.layout.notification_collapsed)
        collapsedView.setTextViewText(
            R.id.timestamp,
            DateUtils.SDF_HOUR.format(System.currentTimeMillis())
        )
        collapsedView.setTextViewText(R.id.content_title, sender.name.split(' ').last())
        if (message.text.isNullOrEmpty()) {
            if (message.isReact) {
                collapsedView.setViewVisibility(R.id.content_text, View.VISIBLE)
                collapsedView.setTextViewText(R.id.content_text, context.getString(R.string.symbol_like))
            } else
                collapsedView.setViewVisibility(R.id.content_text, View.GONE)
        } else {
            collapsedView.setViewVisibility(R.id.content_text, View.VISIBLE)
            collapsedView.setTextViewText(R.id.content_text, message.text)
        }

        collapsedView.setImageViewBitmap(R.id.big_icon, getBitmap(context, sender.avatar, circle = true))
        collapsedView.setImageViewBitmap(R.id.small_icon, getBitmap(context, appIconRes))

        if (message.medias.isNullOrEmpty()) {
            collapsedView.setViewVisibility(R.id.content_image, View.GONE)
        } else {
            collapsedView.setViewVisibility(R.id.content_image, View.VISIBLE)
            collapsedView.setImageViewBitmap(R.id.content_image, getBitmap(context, message.medias!![0].path))
        }
        val notificationId = 202

        val notificationBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_chat))
            .setSmallIcon(appIconRes)
            .setCustomContentView(collapsedView)
//            .setCustomBigContentView(expandedView)  // todo: expand user message in group
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context))
        {
            notify(notificationId, notificationBuilder.build())
        }
    }

    private fun getBitmap(context: Context, imageUrl: Any?, circle: Boolean = false): Bitmap? {
        val futureTarget = Glide.with(context)
            .asBitmap()
            .load(imageUrl)
        if (circle) futureTarget.circleCrop()
        return futureTarget.submit().get()
    }
}
