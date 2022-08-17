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
import com.ln.simplechat.utils.DateUtils
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType


class GridImageAdapter(context: Context, result: List<ChatMedia>?) :
    RecyclerView.Adapter<GridImageAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater
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
        val chooseModel = media.chooseModel
        val path = media.path
        val duration = media.duration
        viewHolder.tvDuration.visibility = if (PictureMimeType.isHasVideo(media.mimeType)) View.VISIBLE else View.GONE
        if (chooseModel == SelectMimeType.ofAudio()) {
            viewHolder.tvDuration.visibility = View.VISIBLE
            viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_audio, 0, 0, 0)
        } else {
            viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_video, 0, 0, 0)
        }
        viewHolder.tvDuration.text = DateUtils.formatDurationTime(duration)
        if (chooseModel == SelectMimeType.ofAudio()) {
            viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder)
        } else {
            Glide.with(viewHolder.itemView.context)
                .load(path)
                .centerCrop()
                .placeholder(R.drawable.ps_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.mImg)
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
        mInflater = LayoutInflater.from(context)
        if (result != null) {
            data.addAll(result)
        }
    }
}
