package it.polito.wa2.group17.catalog.connector

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
class ProductConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.warehouse.uri}")
    private lateinit var uri: String

    fun rateProductById(productId: Long, ratingDto: RatingRequest): Long? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<RatingRequest> = HttpEntity(ratingDto, headers)

        val responseEntity: ResponseEntity<RatingDto> =
            restTemplate.postForEntity("$uri/$productId/rating", requestEntity, RatingDto::class.java)

        System.out.println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body?.id
    }

    fun addProduct(productId: Long, putProductRequest: PutProductRequest) {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<PutProductRequest> = HttpEntity(putProductRequest, headers)

        restTemplate.put("$uri/$productId", requestEntity)
    }
}
