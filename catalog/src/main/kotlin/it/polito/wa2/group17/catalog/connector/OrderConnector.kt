package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.OrderDto
import it.polito.wa2.group17.common.dto.OrderPatchRequest
import it.polito.wa2.group17.common.dto.OrderStatus
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import kotlin.jvm.Throws

@Connector
@Primary
class OrderConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.order.uri}")
    private lateinit var uri: String

    fun getOrdersByUsername(userId: Long?): List<OrderDto>? {
        return restTemplate.getForEntity(
            "$uri/orders", Array<OrderDto>::class.java
        ).body?.toList()?.filter { it.buyerId == userId }
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

    fun getStatus(orderId: Long): OrderDto? {
        return restTemplate.getForEntity(
            "$uri/$orderId", OrderDto::class.java
        ).body
    }


    fun changeStatus(productId: Long, status: OrderPatchRequest): Long? {
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<OrderPatchRequest> = HttpEntity(status, headers)

        val responseEntity: OrderDto? =
            restTemplate.patchForObject("$uri/$productId/rating", requestEntity, OrderDto::class.java)

        return responseEntity?.id
    }

}
