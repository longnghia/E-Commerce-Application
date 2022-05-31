package com.goldenowl.ecommerce.models.data

import com.goldenowl.ecommerce.models.repo.ProductDataSource

class LocalProductsDataSource : ProductDataSource {

    override suspend fun getAllProducts(): List<Product> {
        return listOf()
    }
}
