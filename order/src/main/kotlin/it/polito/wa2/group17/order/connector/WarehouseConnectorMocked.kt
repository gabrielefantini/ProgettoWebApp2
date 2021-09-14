package it.polito.wa2.group17.order.connector

import BuyProductResponse
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.warehouse.mock", name = ["enabled"], havingValue = "true")
class WarehouseConnectorMocked: WarehouseConnector() {
    override fun getProduct(productId: Long): ProductModel?
    = ProductModel("p1",10.0)

    override fun getProductWarehouses(productId: Long): List<WarehouseModel>?
    = listOf(
        WarehouseModel(
            1,
            listOf(StoredProductModel(productId,20))
        ),
        WarehouseModel(
            2,
            listOf(StoredProductModel(productId,30))
        )
    )

    override fun buyProduct(warehouseId: Long, productBuyRequest: ProductBuyRequest): BuyProductResponse? =
        BuyProductResponse(
            productBuyRequest.productID,
            20 - productBuyRequest.quantity,
            warehouseId
        )
    override fun updateProductQuantity(warehouseId: Long, productId: Long, updateProductRequest: UpdateProductRequest): StoredProductModel? =
        StoredProductModel(
            productId,
            updateProductRequest.quantity ?: 0
        )
}