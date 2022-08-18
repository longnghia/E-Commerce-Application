package com.ln.simplechat.ui.preview

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ln.simplechat.model.ChatMedia
import com.luck.picture.lib.config.SelectMimeType

class PreviewPagerAdapter(
    private val context: Context,
    private val position: Int,
    private val data: ArrayList<ChatMedia>
) :
    RecyclerView.Adapter<PreviewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        return PreviewHolder(parent)
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        val binding = holder.binding
        val media = data[position]
        when (media.chooseModel) {
            SelectMimeType.ofVideo() -> {}
            SelectMimeType.ofAudio() -> {
                val videoView = VideoView(context)
                videoView.setVideoURI(Uri.parse(media.path))
            }
            else -> {
                Glide.with(context)
                    .load(media.path)
                    .into(binding.previewImage)
            }
        }
    }

    override fun getItemCount() = data.size
}
