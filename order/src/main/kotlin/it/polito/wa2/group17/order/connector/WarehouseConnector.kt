package it.polito.wa2.group17.order.connector

import BuyProductResponse
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class WarehouseConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.warehouse.uri}")
    private lateinit var uri: String

    fun getProduct(productId: Long): ProductModel? {
        return restTemplate
            .getForEntity("$uri/products/${productId}", ProductModel::class.java)
            .body
    }
    fun getProductWarehouses(productId: Long): List<WarehouseModel>? {
        return restTemplate.getForEntity(
            "$uri/products/${productId}/warehouses", Array<WarehouseModel>::class.java
        ).body?.toList() ?: listOf()
    }
    fun buyProduct(warehouseId: Long, productBuyRequest: ProductBuyRequest): BuyProductResponse? {
        return restTemplate.postForEntity(
            "$uri/warehouses/${warehouseId}/sell",productBuyRequest, BuyProductResponse::class.java
        ).body
    }
    fun updateProductQuantity(warehouseId: Long, productId: Long, updateProductRequest: UpdateProductRequest): StoredProductModel? {
        return restTemplate.patchForObject(
            "$uri/warehouses/${warehouseId}/products/${productId}",updateProductRequest, StoredProductModel::class.java
        )
    }
}