package com.ln.simplechat.ui.preview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ln.simplechat.model.ChatMedia

class PreviewPagerAdapter(
    private val data: ArrayList<ChatMedia>
) :
    RecyclerView.Adapter<PreviewHolder>() {

    private val listHolder: MutableList<PreviewHolder> = mutableListOf()

    override fun onViewAttachedToWindow(holder: PreviewHolder) {
        holder.onViewAttachedToWindow()
        super.onViewAttachedToWindow(holder)
    }


    override fun onViewDetachedFromWindow(holder: PreviewHolder) {
        holder.onViewDetachedFromWindow()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val holder = PreviewHolder.getHolder(parent, viewType)
        listHolder.add(holder)
        return holder
    }

    override fun getItemViewType(position: Int): Int {
        val media = data[position]
        return media.getMediaType().ordinal
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        val media = data[position]
        holder.bind(media)
    }

    override fun getItemCount() = data.size

    fun onDestroy() {
        for (holder in listHolder) {
            if (holder is AudioPreviewHolder) {
                holder.releaseAudio()
            }
        }
    }
}