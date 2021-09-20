package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.PostPicture
import it.polito.wa2.group17.common.dto.StoredProductDto
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.warehouse.mock", name = ["enabled"], havingValue = "true")

class WarehouseConnectorMocked: WarehouseConnector() {
    override fun getProducts(): List<StoredProductDto> = listOf(StoredProductDto(0, 10, 5), StoredProductDto(1, 20, 10), StoredProductDto(2, 30, 2))

    override fun getProductById(productId: Long): StoredProductDto? = StoredProductDto(productId, 10, 3)

    override fun getProductPicture(productId: Long): PostPicture? = PostPicture("URL")
}
