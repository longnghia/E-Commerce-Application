package com.goldenowl.ecommerce.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

    fun TextView.setColor(id: Int, default: Int) {
        setTextColor(getColor(this.context, id) ?: default)
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

    private fun glideListener(loadingLayout: View): RequestListener<Drawable> {
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                Log.w("Glide", "onLoadFailed: ${e?.message}")
                loadingLayout.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                loadingLayout.visibility = View.GONE
                return false
            }
        }
    }

    fun TextView.strike(text: String) {
        this.apply {
            this.text = text
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    fun glide2View(imageView: ImageView, loadingLayout: FrameLayout, uri: String) {
        if (uri.contains("https")) {
            Glide
                .with(imageView.context)
                .load(uri)
                .listener(
                    glideListener(loadingLayout)
                )
                .into(imageView)
        } else {
            loadingLayout.visibility = View.GONE
            imageView.setImageURI(Uri.parse(uri))
        }
    }

    fun ViewPager2.autoScroll(interval: Long) {

        val handler = Handler(Looper.getMainLooper())
        var scrollPosition = 0

        val runnable = object : Runnable {

            override fun run() {
                val count = adapter?.itemCount ?: 0
                if (count == 0) {
                    Log.w("autoScroll", "run: Empty list images")
                    return
                }
                setCurrentItem(scrollPosition++ % count, true)
                handler.postDelayed(this, interval)
            }
        }

        registerOnPageChangeCallback(

            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    scrollPosition = position + 1
                }

                override fun onPageScrollStateChanged(state: Int) {
                    // Not necessary
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    // Not necessary
                }
            }
        )

        handler.post(runnable)
    }

    /*https://stackoverflow.com/questions/51141970/check-internet-connectivity-android-in-kotlin*/
    @RequiresApi(Build.VERSION_CODES.M)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return true
            }
        }
        return false
    }


}
