package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.dto.StoredProductDto
import it.polito.wa2.group17.common.connector.Connector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class WarehouseConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.warehouse}")
    private lateinit var uri: String

    fun getProducts(): List<StoredProductDto> {
        return restTemplate.getForEntity(
            "$uri/products", Array<StoredProductDto>::class.java
        ).body?.toList() ?: listOf()
    }

    fun getProductById(productId: Long): StoredProductDto? {
        return restTemplate.getForEntity(
            "$uri/products/$productId", StoredProductDto::class.java
        ).body
    }

    fun getProductPicture(productId: Long): StoredProductDto? {
        // TODO: cosa ritorna?
        return restTemplate.getForEntity(
            "$uri/products/$productId/picture", StoredProductDto::class.java
        ).body
    }
}
