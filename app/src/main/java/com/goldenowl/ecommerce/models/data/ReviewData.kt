package com.goldenowl.ecommerce.models.data


data class ReviewData @JvmOverloads constructor(
    val reviewId: String = "",
    val review: Review = Review(),
    var user: User? = null,
    var helpful: Boolean = false
) {
    override fun toString(): String {
        return "ReviewData(reviewId='$reviewId', review=${review.images.size}, helpful=$helpful)\n"
    }

    companion object {
        fun getStat(listReviewData: List<ReviewData>): MutableMap<Int, Int> {
            var map = mutableMapOf(
                5 to 0,
                4 to 0,
                3 to 0,
                2 to 0,
                1 to 0
            )
            for (review in listReviewData) {
                val rating = review.review.rating
                map[rating] = map[rating]!! + 1
            }
            return map
        }

        fun getAverageRating(listReviewData: List<ReviewData>): Float {
            val map = getStat(listReviewData)
            val averageRating =
                (map[1]!! + 2 * map[2]!! + 3 * map[3]!! + 4 * map[4]!! + 5 * map[5]!!) * 1f / listReviewData.size
            return averageRating
        }
    }
}

