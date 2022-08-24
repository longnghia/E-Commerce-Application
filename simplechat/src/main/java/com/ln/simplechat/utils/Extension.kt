package com.ln.simplechat.utils

import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
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