package com.ln.simplechat.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ln.simplechat.utils.bubble.NotificationHelper

class OnTaskRemoveService : Service() {
    lateinit var notificationHelper: NotificationHelper
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationHelper = NotificationHelper(baseContext)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        notificationHelper.dismissNotification(NotificationHelper.NOTIFY_ID)
        stopSelf()
    }
}