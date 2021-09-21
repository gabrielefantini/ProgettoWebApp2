package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.*

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.warehouse.mock", name = ["enabled"], havingValue = "true")

class WarehouseConnectorMocked: WarehouseConnector() {
    override fun getProducts(): List<StoredProductDto> = listOf(StoredProductDto(0, 10, 5), StoredProductDto(1, 20, 10), StoredProductDto(2, 30, 2))

    override fun getProductById(productId: Long): StoredProductDto? = StoredProductDto(productId, 10, 3)

    override fun getProductPicture(productId: Long): PostPicture? = PostPicture("URL")

    override fun getWalletsByUsername(username: String?) = Wallet(0, 1, mutableSetOf(1, 2, 3), 10.0)

    override fun setProductPicture(productId: Long, picture: PostPicture) = ProductDto(0, "prod", "desc", "URL", "cat", 10.0, 5.0, Date.from(Instant.now()))

    override fun patchProductById(productId: Long, product: PatchProductRequest) = ProductDto(0, "prod", "desc", "URL", "cat", 10.0, 5.0, Date.from(Instant.now()))
}
