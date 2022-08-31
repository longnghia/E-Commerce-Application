package com.ln.simplechat.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ln.simplechat.R
import com.ln.simplechat.model.ChatMedia
import com.luck.picture.lib.entity.LocalMedia

fun LocalMedia.toChatMedia() = ChatMedia(availablePath, duration, mimeType, fileName)

fun Fragment.buildMenu(v: View, @MenuRes menuRes: Int): PopupMenu {
    val popup = PopupMenu(v.context, v)
    val iconMarginPx = resources.getDimension(R.dimen.popup_icon_margin).toInt()
    popup.menuInflater.inflate(menuRes, popup.menu)
    if (popup.menu is MenuBuilder) {
        val menuBuilder = popup.menu as MenuBuilder
        menuBuilder.setOptionalIconsVisible(true)

        for (item in menuBuilder.visibleItems) {
            if (item.icon != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                } else {
                    item.icon =
                        object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                            override fun getIntrinsicWidth(): Int {
                                return intrinsicHeight + iconMarginPx + iconMarginPx
                            }
                        }
                }
            }
        }
    }
    return popup
}

const val REQUEST_CODE_BUBBLES_PERMISSION = 200
const val NOTIFICATION_BUBBLE_RESOLVE = "notification_bubbles"

fun Context.canDeviceDisplayBubbles(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return false
    }
    val bubblesEnabledGlobally: Boolean = try {
        Settings.Global.getInt(contentResolver, NOTIFICATION_BUBBLE_RESOLVE) == 1
    } catch (e: Settings.SettingNotFoundException) {
        false
    }
    return bubblesEnabledGlobally
}

fun Context.getBubblePreference(): Int {
    val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
    return notificationManager.bubblePreference
}

fun Context.areBubblesAllowed(): Boolean {
    return canDeviceDisplayBubbles() && getBubblePreference() != NotificationManager.BUBBLE_PREFERENCE_NONE
}

fun Activity.requestBubblePermissions() {
    startActivityForResult(
        Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
        REQUEST_CODE_BUBBLES_PERMISSION
    )
}

fun Fragment.requestBubblePermissions() {
    requireActivity().requestBubblePermissions()
}

fun View.fadeIn(duration: Long) {
    alpha = 0f
    animate().setDuration(duration).alpha(1f)
}

fun ImageView.setImageUrl(url: String, loadingLayout: FrameLayout? = null, centerCrop: Boolean = false) {
    Glide
        .with(context)
        .load(url)
        .listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.w("Glide", "onLoadFailed: ${e?.message}")
                    loadingLayout?.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadingLayout?.visibility = View.GONE
                    return false
                }
            }
        )
        .apply(
            RequestOptions().apply {
                if (centerCrop) centerCrop()
                error(R.drawable.ps_image_placeholder)
            }
        )
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}