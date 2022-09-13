package com.ln.simplechat.ui.chat

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.ViewCompat
import com.ln.simplechat.R

typealias OnImageAddedListener = (contentUri: Uri, mimeType: String, label: String) -> Unit

private val SUPPORTED_MIME_TYPES = arrayOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif"
)

/**
 * A custom EditText with the ability to handle copy & paste of texts and images. This also works
 * with a software keyboard that can insert images.
 */
class ChatEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var onImageAddedListener: OnImageAddedListener? = null

    init {
        ViewCompat.setOnReceiveContentListener(this, SUPPORTED_MIME_TYPES) { _, payload ->
            val (content, remaining) = payload.partition { it.uri != null }
            if (content != null) {
                val clip = content.clip
                val mimeType = SUPPORTED_MIME_TYPES.find { clip.description.hasMimeType(it) }
                if (mimeType != null && clip.itemCount > 0) {
                    onImageAddedListener?.invoke(
                        clip.getItemAt(0).uri,
                        mimeType,
                        clip.description.label.toString()
                    )
                }
            }
            remaining
        }
    }

    /**
     * Sets a listener to be called when a new image is added. This might be coming from copy &
     * paste or a software keyboard inserting an image.
     */
    fun setOnImageAddedListener(listener: OnImageAddedListener?) {
        onImageAddedListener = listener
    }
}
