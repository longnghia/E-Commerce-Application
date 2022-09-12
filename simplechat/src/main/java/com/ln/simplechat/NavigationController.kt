package com.ln.simplechat

import androidx.fragment.app.Fragment

interface NavigationController {
    val currentUserId: String?
    fun openChat(channelId: String, bubble: Boolean = false, addBackTrack:Boolean= true)
    fun openBoard()
}

fun Fragment.getNavigationController() = requireActivity() as NavigationController
