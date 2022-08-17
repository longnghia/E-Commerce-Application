package com.ln.simplechat.utils

import android.content.Context
import com.ln.simplechat.R
import com.luck.picture.lib.utils.ValueOf
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


object DateUtils {
    private val SF: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")

    private val SDF: SimpleDateFormat = SimpleDateFormat("yyyy-MM")

    private val SDF_YEAR: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val currentTimeMillis: Long
        get() {
            val timeToString = ValueOf.toString(System.currentTimeMillis())
            return ValueOf.toLong(if (timeToString.length > 10) timeToString.substring(0, 10) else timeToString)
        }

    fun getDataFormat(context: Context, time: Long): String {
        var time = time
        time = if (time.toString().length > 10) time else time * 1000
        return if (isThisWeek(time)) {
            context.getString(R.string.sc_current_week)
        } else if (isThisMonth(time)) {
            context.getString(R.string.sc_current_month)
        } else {
            SDF.format(time)
        }
    }

    fun getYearDataFormat(time: Long): String {
        var time = time
        time = if (time.toString().length > 10) time else time * 1000
        return SDF_YEAR.format(time)
    }

    private fun isThisWeek(time: Long): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        val currentWeek: Int = calendar.get(Calendar.WEEK_OF_YEAR)
        calendar.setTime(Date(time))
        val paramWeek: Int = calendar.get(Calendar.WEEK_OF_YEAR)
        return paramWeek == currentWeek
    }

    fun isThisMonth(time: Long): Boolean {
        val date = Date(time)
        val param: String = SDF.format(date)
        val now: String = SDF.format(Date())
        return param == now
    }


    fun millisecondToSecond(duration: Long): Long {
        return duration / 1000 * 1000
    }

    fun dateDiffer(d: Long): Int {
        return try {
            val l1 = currentTimeMillis
            val interval = l1 - d
            Math.abs(interval).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    fun formatDurationTime(timeMs: Long): String {
        var timeMs = timeMs
        val prefix = if (timeMs < 0) "-" else ""
        timeMs = abs(timeMs)
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) java.lang.String.format(
            Locale.getDefault(),
            "%s%d:%02d:%02d",
            prefix,
            hours,
            minutes,
            seconds
        ) else java.lang.String.format(Locale.getDefault(), "%s%02d:%02d", prefix, minutes, seconds)
    }

    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + SF.format(millis)
    }

    val createFileName: String
        get() {
            val millis = System.currentTimeMillis()
            return SF.format(millis)
        }
}
