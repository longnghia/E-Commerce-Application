package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Product

interface ProductDataSource {
    suspend fun getAllProducts():List<Product>
}
