package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@Connector
@Primary
class ProductConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.warehouse.uri}")
    private lateinit var uri: String

    fun rateProductById(productId: Long, ratingDto: RatingRequest): Long? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<RatingRequest> = HttpEntity(ratingDto, headers)

        val responseEntity: ResponseEntity<Long> =
            restTemplate.postForEntity("$uri/products/$productId/rating", requestEntity, Long::class.java)

        println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body
    }

    fun addProduct(newProductRequest: NewProductRequest): ProductDto? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<NewProductRequest> = HttpEntity(newProductRequest, headers)

        return restTemplate.postForEntity("$uri/products", requestEntity,ProductDto::class.java)
            .body

    }

    fun deleteProduct(productId: Long): Long {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        restTemplate.delete("$uri/products/$productId")

        return productId
    }
}
