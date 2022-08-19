package com.ln.simplechat.ui.preview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.model.ChatMedia

class PreviewPagerAdapter(
    private val data: ArrayList<ChatMedia>
) :
    RecyclerView.Adapter<PreviewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        return PreviewHolder.getHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val media = data[position]
        return media.getMediaType()?.ordinal ?: 0
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        val media = data[position]
        holder.bind(media)
    }

    override fun getItemCount() = data.size
}
