//package com.ln.simplechat.ui.chat
//
//import android.content.Context
//import android.net.Uri
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.ln.simplechat.R
//import com.ln.simplechat.utils.DateUtils
//import com.luck.picture.lib.config.PictureMimeType
//import com.luck.picture.lib.config.SelectMimeType
//
//class GridImageMessageAdapter(context: Context, urls: List<String>?) :
//    RecyclerView.Adapter<GridImageMessageAdapter.ViewHolder>() {
//    private val mInflater: LayoutInflater
//    val data: ArrayList<String> = ArrayList()
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        var mImg: ImageView
//        var tvDuration: TextView
//
//        init {
//            mImg = view.findViewById(R.id.fiv)
//            tvDuration = view.findViewById(R.id.tv_duration)
//        }
//    }
//
//    override fun getItemCount() = data.size
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
//        val view: View = mInflater.inflate(R.layout.layout_filter_image, viewGroup, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//        val media = data[position]
//        val chooseModel = media.chooseModel
//        val path = media.availablePath
//        val duration = media.duration
//        viewHolder.tvDuration.visibility = if (PictureMimeType.isHasVideo(media.mimeType)) View.VISIBLE else View.GONE
//        if (chooseModel == SelectMimeType.ofAudio()) {
//            viewHolder.tvDuration.visibility = View.VISIBLE
//            viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_audio, 0, 0, 0)
//        } else {
//            viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ps_ic_video, 0, 0, 0)
//        }
//        viewHolder.tvDuration.setText(DateUtils.formatDurationTime(duration))
//        if (chooseModel == SelectMimeType.ofAudio()) {
//            viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder)
//        } else {
//            Glide.with(viewHolder.itemView.context)
//                .load(if (PictureMimeType.isContent(path) && !media.isCut && !media.isCompressed) Uri.parse(path) else path)
//                .centerCrop()
//                .placeholder(R.drawable.ps_audio_placeholder)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(viewHolder.mImg)
//        }
//        if (mItemClickListener != null) {
//            viewHolder.itemView.setOnClickListener { v: View? ->
//                val adapterPosition = viewHolder.absoluteAdapterPosition
//                mItemClickListener!!.onItemClick(v, adapterPosition)
//            }
//        }
//        if (mItemLongClickListener != null) {
//            viewHolder.itemView.setOnLongClickListener { v: View? ->
////                val adapterPosition = viewHolder.absoluteAdapterPosition
////                mItemLongClickListener!!.onItemLongClick(viewHolder, adapterPosition, v)
//                true
//            }
//        }
//    }
//
//    private var mItemClickListener: OnItemClickListener? = null
//    fun setOnItemClickListener(l: OnItemClickListener?) {
//        mItemClickListener = l
//    }
//
//    interface OnItemClickListener {
//        /**
//         * Item click event
//         *
//         * @param v
//         * @param position
//         */
//        fun onItemClick(v: View?, position: Int)
//
//        /**
//         * Open PictureSelector
//         */
//        fun openPicture()
//    }
//
//    private var mItemLongClickListener: AdapterView.OnItemLongClickListener? = null
//    fun setItemLongClickListener(l: AdapterView.OnItemLongClickListener?) {
//        mItemLongClickListener = l
//    }
//
//    companion object {
//        const val TAG = "PictureSelector"
//    }
//
//    init {
//        mInflater = LayoutInflater.from(context)
//        if (urls != null) {
//            data.addAll(urls)
//        }
//    }
//}
