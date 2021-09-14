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
            .getForEntity("$uri/products/{}", ProductModel::class.java, productId)
            .body
    }
    fun getProductWarehouses(productId: Long): List<WarehouseModel>? {
        return restTemplate.getForEntity(
            "$uri/products/{}/warehouses", Array<WarehouseModel>::class.java, productId
        ).body?.toList()
    }
    fun buyProduct(warehouseId: Long, productBuyRequest: ProductBuyRequest): BuyProductResponse? {
        return restTemplate.postForEntity(
            "$uri/warehouses/{}/sell",productBuyRequest, BuyProductResponse::class.java, warehouseId
        ).body
    }
    fun updateProductQuantity(warehouseId: Long, productId: Long, updateProductRequest: UpdateProductRequest): StoredProductModel? {
        return restTemplate.patchForObject(
            "$uri/warehouses/{}/products/{}",updateProductRequest, StoredProductModel::class.java, warehouseId, productId
        )
    }
}