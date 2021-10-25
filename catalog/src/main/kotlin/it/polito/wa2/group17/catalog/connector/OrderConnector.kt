package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.*
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

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

    fun addOrder(order: NewOrderRequest, user: UserDetailsDto): Long {
        val orderReq = OrderRequest(
            userId = user.id!!,
            deliveryAddr = user.deliveryAddr,
            email = user.email,
            productOrders = order.productOrders
        )
        return restTemplate
            .postForEntity("$uri/orders",orderReq,OrderDto::class.java)
            .body?.id ?:0
    }

    /*
    fun addWalletTransaction(transaction: TransactionModel, walletId: Long): TransactionModel? {
        return restTemplate
            .postForEntity("$uri/wallets/${walletId}/transactions",transaction, TransactionModel::class.java)
            .body
    }
    */

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

    fun changeStatus(orderId: Long, status: OrderPatchRequest,userId: Long): Long? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<OrderPatchRequest> = HttpEntity(status, headers)

        val responseEntity: OrderDto? =
            restTemplate.patchForObject("$uri/orders/$orderId?userId=$userId", requestEntity, OrderDto::class.java)

        return responseEntity?.id
    }

}
