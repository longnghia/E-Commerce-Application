package com.goldenowl.ecommerce

import android.content.Context
import android.content.Intent
import com.goldenowl.ecommerce.ui.home.MainActivity

public fun launchHome(context: Context) {
    val homeIntent = Intent(context, MainActivity::class.java)
    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(homeIntent)
}
