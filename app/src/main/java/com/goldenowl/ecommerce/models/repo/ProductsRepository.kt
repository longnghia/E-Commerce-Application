package com.goldenowl.ecommerce.models.repo

import com.goldenowl.ecommerce.models.data.Product

class ProductsRepository(
    private val remoteProductDataSource: ProductDataSource,
    private val localProductDataSource: ProductDataSource
) : ProductRepositoryInterface {
    override fun getCategoryList() {
    }

    override suspend fun getAllProducts(): List<Product> {
        return remoteProductDataSource.getAllProducts()
    }

}
