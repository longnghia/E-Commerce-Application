package com.goldenowl.ecommerce.utils

import android.content.Context
import android.content.Intent
import com.goldenowl.ecommerce.ui.global.MainActivity
import java.text.SimpleDateFormat
import java.util.*

fun launchHome(context: Context) {
    val homeIntent = Intent(context, MainActivity::class.java)
    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(homeIntent)
}

fun getDateTime(time: Long?): String {
    if (time == null) {
        return ""
    }
    val sdf = SimpleDateFormat("MM/dd/yyyy")
    val netDate = Date(time)
    return sdf.format(netDate)
}