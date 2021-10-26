package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.OrderDto
import it.polito.wa2.group17.common.dto.OrderPatchRequest
import it.polito.wa2.group17.common.dto.OrderStatus
import it.polito.wa2.group17.common.dto.ProductOrderModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@ConditionalOnProperty(prefix = "connectors.order.mock", name = ["enabled"], havingValue = "true")
class OrderConnectorMocked: OrderConnector() {
    override fun getOrderById(orderId: Long): OrderDto? = OrderDto(orderId, 1, listOf(ProductOrderModel(0, 10, 15.0), ProductOrderModel(1, 20, 100.0)), 2150.0, OrderStatus.ISSUED);

    /*
    override fun cancelOrder(orderId: Long) {
        print("Order $orderId cancelled.")
    }

     */

    /*override fun addOrder(order: OrderDto): Long {
       print("Order ${order.id} added")
        return order.id
    }*/

    //override fun getOrdersByUsername(userId: Long?): List<OrderDto> = listOf(OrderDto(0, 1, listOf(ProductOrderModel(0, 10, 15.0), ProductOrderModel(1, 20, 100.0)), 2150.0, OrderStatus.ISSUED))

    //override fun getStatus(orderId: Long) = OrderDto(1, 2, listOf(), 0.0, OrderStatus.DELIVERED)

    //override fun changeStatus(productId: Long, status: OrderPatchRequest) = 0L
}
