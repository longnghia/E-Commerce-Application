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
import android.util.Log
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
import com.ln.simplechat.model.Chat
import com.ln.simplechat.model.Member
import com.ln.simplechat.model.Message
import com.ln.simplechat.ui.bubble.BubbleActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    companion object {

        val listMember = listOf(
            Member(
                "DPql1uxYezTe4m6HrP0UMlm3Ikh2",
                "Paul",
                "Paul@gmail.com",
                "",
                "https://firebasestorage.googleapis.com/v0/b/e-commerce-android-application.appspot.com/o/images%2Favatar%2Fdefault?alt=media&token=d1a8036a-345b-4ed0-8ef3-fadb47e78380"
            ),
            Member(
                "PVKt9L4g67VR8ExdWji9MYM8eII2",
                "User1",
                "User1@gmail.com",
                "",
                "https://firebasestorage.googleapis.com/v0/b/e-commerce-android-application.appspot.com/o/images%2Favatar%2Fdefault?alt=media&token=d1a8036a-345b-4ed0-8ef3-fadb47e78380"
            ),
        )

        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */
        const val CHANNEL_NEW_MESSAGES = "new_messages"

        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
        const val NOTIFY_ID = 1337
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
        var shortcuts = listMember.map { contact ->
            val icon = IconCompat.createWithAdaptiveBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_account_circle_black_36dp)
//                        context.resources.assets.open(contact.avatar).use { input ->
//                    BitmapFactory.decodeStream(input)
//        }
            )
            // Create a dynamic shortcut for each of the contacts.
            // The same shortcut ID will be used when we show a bubble notification.
            val info = ShortcutInfoCompat.Builder(context, contact.getShortcutId())
                .setLocusId(LocusIdCompat(contact.getShortcutId()))
                .setActivity(ComponentName(context, SimpleChatActivity::class.java))
                .setShortLabel(contact.name)
                .setIcon(icon)
                .setLongLived(true)
                .setCategories(setOf("com.ln.simplechat.category.TEXT_SHARE_TARGET"))
                .setIntent(
                    Intent(context, SimpleChatActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(
                            Uri.parse(
                                "https://android.example.com/chat/${contact.id}"
                            )
                        )
                )
                .setPerson(
                    Person.Builder()
                        .setName(contact.name)
                        .setIcon(icon)
                        .build()
                )
                .build()

            Log.d("0000", "updateShortcuts: $info")
            info
        }
        // Move the important contact to the front of the shortcut list.
//        if (importantContact != null) {
//            shortcuts = shortcuts.sortedByDescending { it.id == importantContact.shortcutId }
//        }

        // Truncate the list if we can't show all of our contacts.
        val maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
        if (shortcuts.size > maxCount) {
            shortcuts = shortcuts.take(maxCount)
        }
        for (shortcut in shortcuts) {
            val res = if (ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)) "success" else "false"
            Log.d("0000", "updateShortcuts: ${shortcut.id} $res")
        }
        Log.d("0000", "can bubble: ${canBubble(listMember[0])}, ${canBubble(listMember[1])}")
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
    fun showNotification(chat: Chat, fromUser: Boolean, update: Boolean = false) {
        updateShortcuts()
        val icon = IconCompat.createWithAdaptiveBitmap(
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_account_circle_black_36dp)
        )
//        val icon = IconCompat.createWithAdaptiveBitmapContentUri(chat.member.avatar)
        val user = Person.Builder().setName(context.getString(R.string.sender_you)).build()
        val person = Person.Builder().setName(chat.member.name).setIcon(icon).build()
        val contentUri = "https://android.example.com/chat/${chat.channelId}".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            // Launch BubbleActivity as the expanded bubble.
            Intent(context, BubbleActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(contentUri),
            flagUpdateCurrent(mutable = true)
        )
        // Let's add some more content to the notification in case it falls back to a normal
        // notification.
        val messagingStyle = NotificationCompat.MessagingStyle(user)
        val messages = listOf(
            Chat(
                listMember[0],
                Message("DPql1uxYezTe4m6HrP0UMlm3Ikh2", "123"),
                "123"
            ),
            Chat(
                listMember[1],
                Message("User1", "456"),
                "123"
            ),
        )
        for (chat in messages) {
            val message = chat.message
            val member = chat.member
            val m = NotificationCompat.MessagingStyle.Message(
                message.text,
                message.timestamp,
                if (message.sender == "DPql1uxYezTe4m6HrP0UMlm3Ikh2") person else null
            ).apply {
                if (member.avatar != null) {
//                    setData(message.photoMimeType, member.avatar)
                }
            }
            if (chat != messages[1]) {
                messagingStyle.addHistoricMessage(m)
            } else {
                messagingStyle.addMessage(m)
            }
        }

        val bubble = NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
            .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
            .apply {
                if (fromUser) setAutoExpandBubble(true)
                if (fromUser || update) setSuppressNotification(true)
            }
            .build()


        Log.d("0000", "showNotification: ${chat.member}")
        val builder = NotificationCompat.Builder(context, CHANNEL_NEW_MESSAGES)
            .setBubbleMetadata(bubble)
            .setContentTitle(chat.member.name)
            .setSmallIcon(R.drawable.ic_account_circle_black_36dp)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(chat.member.getShortcutId())
            .setLocusId(LocusIdCompat(chat.member.getShortcutId()))
            .addPerson(person)
            .setShowWhen(true)
            // The content Intent is used when the user clicks on the "Open Content" icon button on
            // the expanded bubble, as well as when the fall-back notification is clicked.
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
            // Direct Reply
//            .addAction(
//                NotificationCompat.Action
//                    .Builder(
//                        IconCompat.createWithResource(context, R.drawable.ic_send),
//                        context.getString(R.string.label_reply),
//                        PendingIntent.getBroadcast(
//                            context,
//                            REQUEST_CONTENT,
//                            Intent(context, ReplyReceiver::class.java).setData(contentUri),
//                            flagUpdateCurrent(mutable = true)
//                        )
//                    )
//                    .addRemoteInput(
//                        RemoteInput.Builder(ReplyReceiver.KEY_TEXT_REPLY)
//                            .setLabel(context.getString(R.string.hint_input))
//                            .build()
//                    )
//                    .setAllowGeneratedReplies(true)
//                    .build()
//            )
            // Let's add some more content to the notification in case it falls back to a normal
            // notification.
            .setStyle(messagingStyle)
//            .setWhen(chat.messages.last().timestamp)
        // Don't sound/vibrate if an update to an existing notification.
        if (update) {
            builder.setOnlyAlertOnce(true)
        }
        notificationManager.notify(NOTIFY_ID, builder.build())
    }

    fun dismissNotification(id: Long) {
        notificationManager.cancel(id.toInt())
    }

    fun canBubble(contact: Member): Boolean {
        val channel = notificationManager.getNotificationChannel(
            CHANNEL_NEW_MESSAGES,
            contact.getShortcutId()
        )
        return notificationManager.areBubblesAllowed() || channel?.canBubble() == true
    }

    fun updateNotification(chat: Chat, chatId: Long, prepopulatedMsgs: Boolean) {
        if (!prepopulatedMsgs) {
            // Update notification bubble metadata to suppress notification so that the unread
            // message badge icon on the collapsed bubble is removed.
            showNotification(chat, fromUser = false, update = true)
        } else {
            dismissNotification(chatId)
        }
    }
}

