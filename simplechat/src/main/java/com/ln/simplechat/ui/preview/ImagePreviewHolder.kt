package com.ln.simplechat.ui.preview

import com.bumptech.glide.Glide
import com.ln.simplechat.databinding.LayoutPreviewImageBinding
import com.ln.simplechat.model.ChatMedia

class ImagePreviewHolder(binding: LayoutPreviewImageBinding) : PreviewHolder(binding.root) {
    private val previewImage = binding.previewImage

    override fun bind(media: ChatMedia) {
        Glide.with(itemView.context)
            .load(media.path)
            .into(previewImage)
    }
}
