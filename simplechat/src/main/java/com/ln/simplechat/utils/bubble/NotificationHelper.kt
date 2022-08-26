package com.ln.simplechat.utils.bubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.LocusIdCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.ln.simplechat.R
import com.ln.simplechat.SimpleChatActivity
import com.ln.simplechat.model.Channel
import com.ln.simplechat.model.Message
import com.ln.simplechat.ui.bubble.BubbleActivity
import com.ln.simplechat.utils.areBubblesAllowed
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject


class NotificationHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    lateinit var channel: Channel
    var readyToBubble = false
    fun initData(channel: Channel) {
        this.channel = channel
        readyToBubble = true
    }

    companion object {
        const val CHANNEL_NEW_MESSAGES = "new_messages"

        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
        const val NOTIFY_ID: Int = 1337
    }

    private val notificationManager: NotificationManager =
        context.getSystemService() ?: throw IllegalStateException()

    fun setUpNotificationChannels() {
        if (notificationManager.getNotificationChannel(CHANNEL_NEW_MESSAGES) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_NEW_MESSAGES,
                    context.getString(R.string.channel_new_messages),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_new_messages_description)
                }
            )
        }
        updateShortcuts()
    }

    @WorkerThread
    fun updateShortcuts() {
        var shortcut = channel.let { channel ->
            val groupAvatarPath = context.filesDir.absolutePath + "/${channel.id}"
            val groupAvatarFile = File(groupAvatarPath)
            val icon = IconCompat.createWithAdaptiveBitmapContentUri(Uri.fromFile(groupAvatarFile))
            val info = ShortcutInfoCompat.Builder(context, channel.getShortcutId())
                .setLocusId(LocusIdCompat(channel.getShortcutId()))
                .setActivity(ComponentName(context, SimpleChatActivity::class.java))
                .setShortLabel(channel.name)
                .setIcon(icon)
                .setLongLived(true)
                .setCategories(setOf("com.ln.simplechat.category.TEXT_SHARE_TARGET"))
                .setIntent(
                    Intent(context, SimpleChatActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(
                            Uri.parse(
                                "https://android.example.com/chat/${channel.id}"
                            )
                        )
                )
                .setPerson(
                    Person.Builder()
                        .setName(channel.name)
                        .setIcon(icon)
                        .build()
                )
                .build()
            info
        }

        val maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
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

    @WorkerThread
    fun showNotification(message: Message, fromUser: Boolean, update: Boolean = false) {
        updateShortcuts()
        val groupAvatarPath = context.filesDir.absolutePath + "/${channel.id}"
        val groupAvatarFile = File(groupAvatarPath)
        var icon: IconCompat = if (groupAvatarFile.exists()) {
            IconCompat.createWithAdaptiveBitmap(BitmapFactory.decodeFile(groupAvatarPath))
        } else IconCompat.createWithAdaptiveBitmap(
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_create_group)
        )
        val user = Person.Builder().setName(context.getString(R.string.sender_you)).build()
        val person = Person.Builder().setName(message.sender).setIcon(icon).build()
        val contentUri = "https://android.example.com/chat/${channel.id}".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            Intent(context, BubbleActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(contentUri),
            flagUpdateCurrent(mutable = true)
        )
        val messagingStyle = NotificationCompat.MessagingStyle(user)

        val bubble = NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
            .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
            .apply {
                if (fromUser) setAutoExpandBubble(true)
                if (fromUser || update) setSuppressNotification(true)
            }
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_MESSAGES)
            .setBubbleMetadata(bubble)
            .setContentTitle(message.sender)
            .setSmallIcon(R.drawable.ic_account_circle_black_36dp)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(channel.getShortcutId())
            .setLocusId(LocusIdCompat(channel.getShortcutId()))
            .addPerson(person)
            .setShowWhen(true)

            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, SimpleChatActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    flagUpdateCurrent(mutable = false)
                )
            )
            .setStyle(messagingStyle)
        if (update) {
            builder.setOnlyAlertOnce(true)
        }

        notificationManager.notify(NOTIFY_ID, builder.build())
    }

    fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun canBubble(shortcutId: String): Boolean {
        val channel = notificationManager.getNotificationChannel(
            CHANNEL_NEW_MESSAGES,
            shortcutId
        )
        return context.areBubblesAllowed() || channel?.canBubble() == true
    }

    fun updateNotification(message: Message, chatId: Long, prepopulatedMsgs: Boolean) {
        if (!prepopulatedMsgs) {
            showNotification(message, fromUser = false, update = true)
        } else {
            dismissNotification(NOTIFY_ID)
        }
    }

}

