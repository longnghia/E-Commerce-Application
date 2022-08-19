package com.ln.simplechat.ui.preview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.databinding.LayoutPreviewAudioBinding
import com.ln.simplechat.databinding.LayoutPreviewImageBinding
import com.ln.simplechat.databinding.LayoutPreviewVideoBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.MediaType

abstract class PreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(media: ChatMedia)

    companion object {
        fun getHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
            return when (viewType) {
                MediaType.IMAGE.ordinal -> ImagePreviewHolder(
                    LayoutPreviewImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
                MediaType.VIDEO.ordinal -> VideoPreviewHolder(
                    LayoutPreviewVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
                MediaType.AUDIO.ordinal -> AudioPreviewHolder(
                    LayoutPreviewAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
                else -> ImagePreviewHolder(
                    LayoutPreviewImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }
    }
}
