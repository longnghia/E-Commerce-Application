package com.goldenowl.ecommerce.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.ReviewImagesAdapter
import com.goldenowl.ecommerce.databinding.ItemReviewBinding
import com.goldenowl.ecommerce.models.data.ReviewData
import com.goldenowl.ecommerce.utils.Utils
import com.goldenowl.ecommerce.utils.Utils.setColor
import java.text.SimpleDateFormat


class ReviewAdapter(private val listener: IClickListener) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    private var mListReviewData = listOf<ReviewData>()

    fun setData(listReviewData: List<ReviewData>) {
        mListReviewData = listReviewData
        notifyDataSetChanged()
    }

    interface IClickListener {
        fun onHelpfulClick(reviewData: ReviewData)
    }


    inner class ViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reviewData: ReviewData, position: Int) {

            val review = reviewData.review
            val user = reviewData.user

            if (reviewData.helpful) {
                binding.ivThumbUp.isSelected = true
                binding.tvHelpful.setColor(R.color.red_dark, 0xDB3022)
            } else {
                binding.ivThumbUp.isSelected = false
                binding.tvHelpful.setColor(R.color.grey_text, 0x9B9B9B)
            }
            binding.layoutHelpful.setOnClickListener {
                reviewData.helpful = !(reviewData.helpful)
                notifyItemChanged(position)
                listener.onHelpfulClick(reviewData)
            }
            binding.productRatingBar.rating = review.rating.toFloat()
            binding.tvRatingDate.text = SimpleDateFormat("MMMM dd, YYYY").format(review.date)
            binding.tvUserName.text = user?.name ?: ""
            Utils.glide2View(binding.ivUserAvatar, binding.layoutLoading.loadingFrameLayout, user?.avatar ?: "")

            if (review.images.isEmpty()) {
                binding.rcvImgs.adapter = null
                binding.rcvImgs.visibility = View.GONE
            } else {
                binding.rcvImgs.visibility = View.VISIBLE
                binding.rcvImgs.adapter = ReviewImagesAdapter(review.images)
            }

            if (review.review.isNullOrBlank()) {
                binding.tvReview.text = ""
                binding.tvReview.visibility = View.GONE
            } else {
                binding.tvReview.text = review.review
                binding.tvReview.visibility = View.VISIBLE
            }
        }
    }


    companion object {
        const val TAG = "ReviewAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewData = mListReviewData[position]
        holder.bind(reviewData, position)
    }

    override fun getItemCount() = mListReviewData.size

}

