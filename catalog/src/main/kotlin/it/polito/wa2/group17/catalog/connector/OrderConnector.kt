package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.OrderDto
import it.polito.wa2.group17.common.dto.OrderStatus
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@Connector
class OrderConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.orders}")
    private lateinit var uri: String

    fun getOrdersByUsername(username: String?): List<OrderDto> {
        // TODO: qual è l'endpoint da chiamare?
        return listOf()
    }

    fun getWalletsByUsername(username: String?): Unit? {
        // TODO /users/{userID}
        return null
    }

    fun addOrder(order: OrderDto): Long {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<OrderDto> = HttpEntity(order, headers)

        val restTemplate = RestTemplate()
        val responseEntity: ResponseEntity<OrderDto> =
            restTemplate.postForEntity("$uri/orders", requestEntity, OrderDto::class.java)

        System.out.println("Status Code: " + responseEntity.statusCode)

        return responseEntity.body?.id?:0L
    }

    fun getOrderById(orderId: Long): OrderDto? {
        return restTemplate.getForEntity(
            "$uri/orders/$orderId", OrderDto::class.java
        ).body
    }

    fun cancelOrder(orderId: Long) {
        val order = restTemplate.getForEntity(
            "$uri/orders/{orderId}", OrderDto::class.java
        ).body
        if (order != null) {
            if (order.status == OrderStatus.ISSUED) {
                restTemplate.delete("$uri/orders/$orderId", null)
            }
            throw GenericBadRequestException("The shipping is started!")
        }
        throw EntityNotFoundException("Order with id $orderId")

    }


}
