package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.dto.ConvertibleDto.Factory.fromEntity
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.repository.UserRepository
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface CatalogService {

    @Throws(EntityNotFoundException::class)
    fun getOrders(): Unit?

    @Throws(EntityNotFoundException::class)
    fun getOrderById(orderId: Long): Unit?

    @Throws(EntityNotFoundException::class)
    fun addNewOrder(order: String/*OrderDto*/): Long

    @Throws(EntityNotFoundException::class)
    fun listProducts(): Unit?

    @Throws(EntityNotFoundException::class)
    fun getProduct(productId: Long): Unit?

    @Throws(EntityNotFoundException::class)
    fun getWallets(): Unit?

    @Throws(EntityNotFoundException::class)
    fun getUserInformation(): UserDetailsDto?

    @Throws(EntityNotFoundException::class)
    fun updateUserInformation(username: String, email: String, name: String, surname: String, deliveryAddr:String): Long?


}


@Service
@Transactional
private open class CatalogServiceImpl(
    //val orderController???
    val userRepository: UserRepository
) : CatalogService {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun getOrders(): Unit? {
        val username = SecurityContextHolder.getContext().authentication.name
        logger.info("Searching for the orders of the user with username {}", username)
        val orders = null //metodo order
        return orders
    }

    override fun getOrderById(orderId: Long): Unit? {
        logger.info("Searching for the order with id {}", orderId)
        val order = null //metodo order
        return order
    }

    override fun addNewOrder(order: String): Long {
        logger.info("Adding a new order...")
        val orderId = 0L //metodo order
        return orderId
    }

    override fun listProducts(): Unit? {
        logger.info("Listing all the products...")
        //TODO
        return null

    }

    override fun getProduct(productId: Long): Unit? {
        logger.info("Searching for product with id {}", productId)
        //TODO
        return null
    }

    override fun getWallets(): Unit? {
        val username = SecurityContextHolder.getContext().authentication.name
        logger.info("Searching for the wallets of the user with username {}", username)
        val wallets = null //metodo order
        return wallets
    }

    override fun getUserInformation(): UserDetailsDto? {
        val username = SecurityContextHolder.getContext().authentication.name
        val userInfo = userRepository.findByUsername(username)
        if(!userInfo.isEmpty) {
            return fromEntity(userInfo.get())
        }
        else return null
    }

    override fun updateUserInformation(username: String, email: String, name: String, surname: String, deliveryAddr:String): Long? {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.updateUserInformation(username, email, name, surname, deliveryAddr)
        if (!user.isEmpty) return user.get().getId()
        else return null
    }


}
