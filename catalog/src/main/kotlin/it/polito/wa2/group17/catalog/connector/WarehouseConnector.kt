package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.catalog.dto.WarehouseDto
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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

    fun setProductPicture(productId: Long, picture: PostPicture):ProductDto? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<PostPicture> = HttpEntity(picture, headers)

        val responseEntity: ResponseEntity<ProductDto> =
            restTemplate.postForEntity("$uri/$productId/picture", requestEntity, ProductDto::class.java)

        System.out.println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body
    }

    fun patchProductById(productId: Long, product: PatchProductRequest): ProductDto? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<PatchProductRequest> = HttpEntity(product, headers)

        val responseEntity: ProductDto? =
            restTemplate.patchForObject("$uri/$productId/picture", requestEntity, ProductDto::class.java)

        return responseEntity
    }

    fun addProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest): StoredProductDto? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<AddProductRequest> = HttpEntity(addProductRequest, headers)

        val responseEntity: ResponseEntity<StoredProductDto> =
            restTemplate.postForEntity("$uri/$warehouseId/products", requestEntity, StoredProductDto::class.java)

        System.out.println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body
    }

    fun addWarehouse(warehouseRequest: WarehouseRequest): Long? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<WarehouseRequest> = HttpEntity(warehouseRequest, headers)

        val responseEntity: ResponseEntity<Long> =
            restTemplate.postForEntity("$uri/", requestEntity, Long::class.java)

        System.out.println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body
    }

    fun deleteWarehouse(warehouseId: Long) {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<Long> = HttpEntity(warehouseId, headers)

        restTemplate.delete("$uri/", requestEntity)
    }
}
