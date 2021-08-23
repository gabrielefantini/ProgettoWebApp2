package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.StoredProductDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Connector
class OrderConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.orders}")
    private lateinit var uri: String

    fun getOrdersByUsername(username: String?): Unit? {
        return null
    }

    fun getWalletsByUsername(username: String?): Unit? {
        return null
    }

    fun addOrder(/*order: OrderDto*/): Long {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val order = null
        // order è un OrderDto, va popolato
        val requestEntity: HttpEntity</*OrderDto*/Unit> = HttpEntity(order, headers)

        val restTemplate = RestTemplate()
        val responseEntity: ResponseEntity<Unit> =
            restTemplate.postForEntity("$uri/orders", requestEntity, Unit::class.java/*OrderDto::class.java*/)

        System.out.println("Status Code: " + responseEntity.statusCode)
        val id = responseEntity.body // prendere l'id

        return 0L
    }


}
