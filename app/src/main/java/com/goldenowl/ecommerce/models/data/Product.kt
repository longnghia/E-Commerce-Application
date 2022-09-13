package com.goldenowl.ecommerce.models.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
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
    val sellerId: String = ""
) : Parcelable {
    override fun toString(): String {
        return "Product(id='$id', title='$title', categoryName='$categoryName', price=${getOriginPrice()} , image=${getImage()}, isPopular=${isPopular}, date=$createdDate)"
    }

    @Exclude
    fun getOriginPrice(): Int {
        if (this.colors.isNotEmpty())
            return colors[0].sizes[0].price
        else {
            throw Exception("No prices found: ${this.colors}")
        }
    }

    @Exclude
    fun getDiscountPrice(): Float {
        return if (this.salePercent != null) {
            getOriginPrice() * (100 - this.salePercent!!) / 100f
        } else
            getOriginPrice().toFloat()
    }

    @Exclude
    fun getImage(): String? {
        if (this.images != null && this.images.isNotEmpty() && this.images[0] != null) {
            return this.images[0]
        }
        return null
    }

    @Exclude
    fun getListSize(): List<Size> {
        if (this.colors.isNullOrEmpty())
            return emptyList()
        return colors[0].sizes
    }

    @Exclude
    fun isAvailable(favorite: Favorite): Boolean {
        val color = colors[0]
        val size = color?.sizes?.find {
            it.size == favorite.size
        }
        val quantity = size?.quantity ?: 0
        return quantity != 0
    }

    @Exclude
    fun getFirstColor(): String {
        return colors[0].color
    }

    @Exclude
    fun getListColor(): List<String> {
        return colors.map {
            it.color
        }
    }

    /**
     * get price by color and size, included discount
     * @param pColor: product color
     * @param pSize: product size
     * @return price: float
     */
    @Exclude
    fun getPriceByColor(pColor: String, pSize: String): Float? {
        colors.find {
            it.color == pColor
        }.also { color ->
            color?.sizes?.find { size ->
                size.size == pSize
            }.also {
                if (it?.price == null)
                    return null
                val price = if (salePercent == null) it?.price else (it?.price?.times((100 - salePercent!!))) / 100f
                return price.toFloat()
            }
        }
    }
    @Exclude
    fun getPriceByCart(cart: Cart): Float? {
        return getPriceByColor(cart.color, cart.size)
    }

    @Parcelize
    data class Color @JvmOverloads constructor(
        var color: String = "",
        var sizes: List<Size> = ArrayList()
    ) : Parcelable

    @Parcelize
    data class Size @JvmOverloads constructor(
        var price: Int = 0,
        var quantity: Int = 0,
        var size: String = ""
    ) : Parcelable

    @Parcelize
    data class Tag @JvmOverloads constructor(
        var id: String = "",
        var name: String = ""
    ) : Parcelable

}

