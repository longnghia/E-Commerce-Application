package services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ln.simplechat.CHAT_BASE_URI
import com.ln.simplechat.R
import com.ln.simplechat.SimpleChatActivity
import com.ln.simplechat.application.SimpleChatApp
import com.ln.simplechat.application.SimpleChatApp.Companion.currentChannel
import com.ln.simplechat.application.toast
import com.ln.simplechat.model.*
import com.ln.simplechat.utils.DateUtils


object ChatFMService {

    private val typeMessage = object : TypeToken<Message>() {}.type
    private val typeChannel = object : TypeToken<Channel>() {}.type
    private val typeMember = object : TypeToken<Member>() {}.type
    val notificationId = 201
    val notifIcon = SimpleChatApp.instance?.applicationInfo?.icon ?: R.drawable.ic_notification

    const val REQUEST_CONTENT = 1
    fun onMessageReceived(context: Context, remoteMessage: RemoteMessage, userId: String) {
        var showNotification: Boolean
        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data.also { data ->
                try {
                    val messageStr = data["message"]
                    val channelStr = data["channel"]
                    val senderStr = data["sender"]
                    val gson: Gson = GsonBuilder().create()
                    val channel: Channel =
                        gson.fromJson(channelStr, typeChannel)
                    val message: Message =
                        gson.fromJson(messageStr, typeMessage)
                    val sender: Member =
                        gson.fromJson(senderStr, typeMember)
                    showNotification = userId != message.sender && channel.id != currentChannel
                    if (showNotification) {
                        notifyCloudMessage(context, sender, channel, message)
                    }
                } catch (e: Exception) {
                    toast(e.message)
                }
            }
        }
    }

    private fun notifyCloudMessage(
        context: Context,
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
        collapsedView.setTextViewText(R.id.content_title, sender.getShortName())

        if (message.isReact) {
            collapsedView.setTextViewText(R.id.content_text, context.getString(R.string.symbol_like))
        } else if (!message.medias.isNullOrEmpty()) {
            collapsedView.setViewVisibility(R.id.content_image, View.VISIBLE)
            collapsedView.setImageViewBitmap(
                R.id.content_image,
                getBitmap(context, message.medias!![0].path, crop = true)
            )

            val mediaSent = buildString {
                append(sender.getShortName())
                append(" ")
                append(context.getString(R.string.notification_sent))
                append(" ")
                append(buildMediaSentNotification(context, message.medias!!))
            }
            collapsedView.setTextViewText(R.id.content_text, mediaSent)
        } else if (!message.text.isNullOrEmpty()) {
            collapsedView.setTextViewText(R.id.content_text, message.text)
        } else
            collapsedView.setTextViewText(R.id.content_text, context.getString(R.string.new_message))

        collapsedView.setImageViewBitmap(R.id.big_icon, getBitmap(context, sender.avatar, circle = true))
        collapsedView.setImageViewBitmap(R.id.small_icon, getBitmap(context, notifIcon))

        val contentUri = "$CHAT_BASE_URI{channel.id}".toUri()

        val notificationBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_chat))
            .setSmallIcon(notifIcon)
            .setCustomContentView(collapsedView)
//            .setCustomBigContentView(expandedView)  // todo: expand user message in group
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, SimpleChatActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    flagUpdateCurrent(mutable = true)
                )
            )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            SimpleChatApp.instance?.applicationInfo?.labelRes?.let {
                notificationBuilder.setContentTitle(
                    context.getString(
                        it
                    )
                )
            }

        with(NotificationManagerCompat.from(context))
        {
            notify(notificationId, notificationBuilder.build())
        }
    }

    private fun buildMediaSentNotification(context: Context, medias: List<ChatMedia>): String {
        var countAudio = 0
        var countImage = 0
        var countVideo = 0
        for (media in medias) {
            when (media.getMediaType()) {
                MediaType.AUDIO -> countAudio++
                MediaType.VIDEO -> countVideo++
                else -> countImage++
            }
        }
        val listCount = listOf(countAudio, countImage, countVideo)
        val count = listCount.filter { it > 0 }.size
        val string = if (count > 1)
            context.resources.getQuantityString(R.plurals.num_media, count, count)
        else {
            if (countAudio > 0)
                context.resources.getQuantityString(R.plurals.num_audio, countAudio, countAudio)
            else if (countVideo > 0)
                context.resources.getQuantityString(R.plurals.num_video, countVideo, countVideo)
            else context.resources.getQuantityString(R.plurals.num_image, countImage, countImage)

        }
        return string
    }

    private fun getBitmap(context: Context, imageUrl: Any?, circle: Boolean = false, crop: Boolean = false): Bitmap? {
        return try {
            val futureTarget = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
            if (circle) futureTarget.circleCrop()
            if (crop) futureTarget.centerCrop()
            futureTarget.submit().get()
        } catch (e: Exception) {
            toast(e)
            null
        }
    }

    private fun flagUpdateCurrent(mutable: Boolean): Int {
        return if (mutable) {
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
    }
}
