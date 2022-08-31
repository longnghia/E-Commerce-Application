package com.ln.simplechat.ui.preview

import android.net.Uri
import android.view.View
import com.ln.simplechat.databinding.LayoutPreviewVideoBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.utils.setImageUrl

class VideoPreviewHolder(binding: LayoutPreviewVideoBinding) : PreviewHolder(binding.root) {
    private val previewVideo = binding.previewVideo
    private val previewImage = binding.previewImage
    private val loading = binding.progress
    private val ivPlayVideo = binding.ivPlayVideo

    override fun bind(media: ChatMedia) {
        previewImage.setImageUrl(media.path)

        ivPlayVideo.apply {
            setOnClickListener { previewVideo.start() }
            visibility = View.INVISIBLE
        }
        previewVideo.apply {

            setVideoURI(Uri.parse(media.path))
            setOnPreparedListener {
                loading.visibility = View.INVISIBLE
                start()
            }
            setOnCompletionListener { ivPlayVideo.visibility = View.VISIBLE }
        }
    }
}
