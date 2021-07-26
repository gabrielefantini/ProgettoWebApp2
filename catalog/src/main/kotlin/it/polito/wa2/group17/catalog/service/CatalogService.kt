package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.repository.UserRepository
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface CatalogService {

    @Throws(EntityNotFoundException::class)
    fun getOrders(email: String): Unit?

    @Throws(EntityNotFoundException::class)
    fun getOrderById(orderId: Long): Unit?


}


@Service
@Transactional
private open class CatalogServiceImpl(
    //val orderController???
) : CatalogService {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun getOrders(email: String): Unit? {
        logger.info("Searching for the orders of the user with email {}", email)
        val orders = null //metodo order
        return orders
    }

    override fun getOrderById(orderId: Long): Unit? {
        logger.info("Searching for the order with id {}", orderId)
        val order = null //metodo order
        return order
    }


}
