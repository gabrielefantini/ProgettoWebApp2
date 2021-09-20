package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.dto.StoredProductDto
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.PostPicture
import it.polito.wa2.group17.common.dto.Wallet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class WarehouseConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.warehouse.uri}")
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

    fun getProductPicture(productId: Long): PostPicture? {
        return restTemplate.getForEntity(
            "$uri/products/$productId/picture", PostPicture::class.java
        ).body
    }

    fun getWalletsByUsername(username: String?): Wallet? {
        return restTemplate.getForEntity(
            "$uri/users/$username", Wallet::class.java
        ).body
    }
}
