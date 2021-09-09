package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.ProductModel
import it.polito.wa2.group17.order.model.StoredProductModel
import it.polito.wa2.group17.order.model.WarehouseModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.users.mock", name = ["enabled"], havingValue = "true")
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

}