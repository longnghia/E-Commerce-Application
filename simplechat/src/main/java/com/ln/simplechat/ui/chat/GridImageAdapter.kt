package com.ln.simplechat.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ln.simplechat.R
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.model.MediaType
import com.ln.simplechat.utils.DateUtils


class GridImageAdapter(private val context: Context, result: List<ChatMedia>?) :
    RecyclerView.Adapter<GridImageAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    val data: ArrayList<ChatMedia> = ArrayList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mImg: ImageView
        var tvDuration: TextView

        init {
            mImg = view.findViewById(R.id.fiv)
            tvDuration = view.findViewById(R.id.tv_duration)
        }
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.layout_filter_image, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val media = data[position]
        val path = media.path

        viewHolder.mImg.contentDescription = media.description

        when (media.getMediaType()) {
            MediaType.AUDIO -> {
                viewHolder.tvDuration.visibility = View.VISIBLE
                viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder)
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_audio, 0, 0, 0)
            }
            MediaType.VIDEO -> {
                viewHolder.tvDuration.visibility = View.VISIBLE
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_video, 0, 0, 0)
                viewHolder.tvDuration.text = DateUtils.formatDurationTime(media.duration)

                Glide.with(context)
                    .load(path)
                    .centerCrop()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.mImg)
            }
            MediaType.IMAGE -> {
                Glide.with(context)
                    .load(path)
                    .centerCrop()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.mImg)
            }
        }

        if (mItemClickListener != null) {
            viewHolder.itemView.setOnClickListener { v: View ->
                val adapterPosition = viewHolder.absoluteAdapterPosition
                mItemClickListener!!.invoke(v, adapterPosition)
            }
        }
        if (mItemLongClickListener != null) {
            viewHolder.itemView.setOnLongClickListener { v: View? ->
                val adapterPosition = viewHolder.absoluteAdapterPosition
                true
            }
        }
    }

    private var mItemClickListener: ((View, Int) -> Unit)? = null
    private var mItemLongClickListener: ((View) -> Unit)? = null

    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        mItemClickListener = listener
    }


    fun setOnItemLongClickListener(listener: (View) -> Unit) {
        mItemLongClickListener = listener
    }

    companion object {
        const val TAG = "PictureSelector"
    }

    init {
        if (result != null) {
            data.addAll(result)
        }
    }
}
