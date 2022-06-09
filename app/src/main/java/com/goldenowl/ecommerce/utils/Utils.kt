package com.goldenowl.ecommerce.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.ui.global.MainActivity
import java.text.SimpleDateFormat
import java.util.*

object Utils {

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

    fun getColor(context: Context, id: Int): Int? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id)
        }
        return null
    }

    fun searchMatch(inputString: String, query: String) {

    }

    interface OnSpanClickListener {
        fun onClick()
    }

    private fun setSpannableText(
        inputString: String,
        start: Int,
        end: Int,
        listener: OnSpanClickListener
    ): SpannableString {
        val ss = SpannableString(inputString)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                listener.onClick()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
//                ds.color = Color.RED
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val boldSpan = StyleSpan(Typeface.BOLD)
        ss.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return ss
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
