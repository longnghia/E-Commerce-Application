package com.goldenowl.ecommerce.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object SimpleDateFormatHelper {
    fun formatDate(date: Date): String {
        return SimpleDateFormat("MMMM dd, YYYY").format(date)
    }

    fun countDate(startDate: Date, endDate: Date): Long {
        val d = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(d, TimeUnit.MILLISECONDS)
    }
}