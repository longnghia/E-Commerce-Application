package com.ln.simplechat

import androidx.fragment.app.Fragment

interface NavigationController {
    fun openChat(channelId: String)
}

fun Fragment.getNavigationController() = requireActivity() as NavigationController
