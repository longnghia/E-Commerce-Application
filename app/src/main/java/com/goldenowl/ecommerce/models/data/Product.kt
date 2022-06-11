package com.goldenowl.ecommerce.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "product_table")
data class Product @JvmOverloads constructor(
    @PrimaryKey
    val id: String = "",
    var title: String = "",
    var brandName: String = "",
    var categoryName: String = "",
    var colors: List<Color> = ArrayList(),
    var createdDate: Date = Date(),
    var description: String = "",
    var images: List<String> = ArrayList(),
    @field:JvmField
    var isPopular: Boolean = false,
    var numberReviews: Int = 0,
    var reviewStars: Int = 0,
    var salePercent: Int? = null,
    var tags: List<Tag> = ArrayList(),
) {
    override fun toString(): String {
        return "Product(id='$id', title='$title', categoryName='$categoryName', price=${getOriginPrice()} , image=${getImage()}, isPopular=${isPopular}, date=$createdDate)"
    }

    fun getOriginPrice(): Int {
        if (this.colors.isNotEmpty())
            return colors[0].sizes[0].price
        else {
            throw Exception("No prices found: ${this.colors}")
        }
    }


    fun getDiscountPrice(): Float {
        return if (this.salePercent != null) {
            getOriginPrice() * (100 - this.salePercent!!) / 100f
        } else
            getOriginPrice().toFloat()
    }

    fun getImage(): String? {
        if (this.images != null && this.images.isNotEmpty() && this.images[0] != null) {
            return this.images[0]
        }
        return null
    }

    fun getListSize(): List<Size> {
        if (this.colors.isNullOrEmpty())
            return emptyList()
        return colors[0].sizes
    }


    data class Color @JvmOverloads constructor(
        var color: String = "",
        var sizes: List<Size> = ArrayList()
    )

    data class Size @JvmOverloads constructor(
        var price: Int = 0,
        var quantity: Int = 0,
        var size: String = ""
    )


    data class Tag @JvmOverloads constructor(
        var id: String = "",
        var name: String = ""
    )
}

