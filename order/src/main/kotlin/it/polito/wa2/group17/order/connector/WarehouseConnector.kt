package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.ProductModel
import it.polito.wa2.group17.order.model.WarehouseModel
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
}