package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.catalog.dto.StoredProductDto
import it.polito.wa2.group17.catalog.dto.WarehouseDto
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
            "$uri/warehouses", Array<WarehouseDto>::class.java
        ).body?.toList()?.flatMap { it.products } ?: listOf()
    }
}
