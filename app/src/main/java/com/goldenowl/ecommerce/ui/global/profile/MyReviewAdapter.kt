package com.goldenowl.ecommerce.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldenowl.ecommerce.adapter.ReviewImagesAdapter
import com.goldenowl.ecommerce.databinding.ItemMyReviewBinding
import com.goldenowl.ecommerce.models.data.Review
import com.goldenowl.ecommerce.utils.SimpleDateFormatHelper


class MyReviewAdapter() :
    RecyclerView.Adapter<MyReviewAdapter.ViewHolder>() {

    private var mListReview = listOf<Review>()

    fun setData(listReview: List<Review>) {
        mListReview = listReview
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMyReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review, position: Int) {

            binding.productRatingBar.rating = review.rating.toFloat()
            binding.tvRatingDate.text = SimpleDateFormatHelper.formatDate(review.date)
            binding.tvProduct.text = review.productId

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
            ItemMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewData = mListReview[position]
        holder.bind(reviewData, position)
    }

    override fun getItemCount() = mListReview.size

}

