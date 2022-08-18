package com.ln.simplechat.ui.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.R
import com.ln.simplechat.databinding.LayoutPreviewBinding

class PreviewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.layout_preview, parent, false)
) {
    val binding = LayoutPreviewBinding.bind(itemView)
}
