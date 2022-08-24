package com.ln.simplechat.utils.bubble

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

const val REQUEST_CODE_BUBBLES_PERMISSION = 200

fun Activity.canDisplayBubbles(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return false
    }
    val bubblesEnabledGlobally: Boolean = try {
        Settings.Global.getInt(getContentResolver(), "notification_bubbles") === 1
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

    Log.d("Activity", "canDisplayBubbles=$bubblesEnabledGlobally")
    return bubblesEnabledGlobally
}
fun Activity.getBubblePreference(): Int {
    val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
    val bubblePreference = notificationManager.bubblePreference
    return bubblePreference
}

fun Activity.requestBubblePermissions() {
    startActivityForResult(
        Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
        REQUEST_CODE_BUBBLES_PERMISSION
    )
}