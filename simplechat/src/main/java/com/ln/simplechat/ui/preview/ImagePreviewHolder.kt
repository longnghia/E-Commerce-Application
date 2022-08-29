package com.ln.simplechat.ui.preview

import com.ln.simplechat.databinding.LayoutPreviewImageBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.utils.setImageUrl

class ImagePreviewHolder(binding: LayoutPreviewImageBinding) : PreviewHolder(binding.root) {
    private val previewImage = binding.previewImage

    override fun bind(media: ChatMedia) {
        previewImage.setImageUrl(media.path)
    }
}
