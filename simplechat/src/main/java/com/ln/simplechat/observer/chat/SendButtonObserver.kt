package com.ln.simplechat.observer.chat

import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import com.ln.simplechat.R

class SendButtonObserver(private val button: ImageView) : TextWatcher {
    override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        if (charSequence.isNullOrBlank()) {
            button.isEnabled = false
            button.setImageResource(R.drawable.outline_send_gray_24)
        } else {
            button.isEnabled = true
            button.setImageResource(R.drawable.outline_send_24)
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable) {}
}

