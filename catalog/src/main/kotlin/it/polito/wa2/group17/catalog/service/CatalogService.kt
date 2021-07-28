package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.connector.OrderConnector
import it.polito.wa2.group17.catalog.connector.WarehouseConnector
import it.polito.wa2.group17.catalog.dto.ConvertibleDto.Factory.fromEntity
import it.polito.wa2.group17.catalog.dto.StoredProductDto
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.repository.UserRepository
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    fun listProducts(): List<StoredProductDto>

    @Throws(EntityNotFoundException::class)
    fun getProduct(productId: Long): StoredProductDto?

    @Throws(EntityNotFoundException::class)
    fun getWallets(): Unit?

    @Throws(EntityNotFoundException::class)
    fun getUserInformation(): UserDetailsDto?

    @Throws(EntityNotFoundException::class)
    fun updateUserInformation(username: String, email: String, name: String, surname: String, deliveryAddr:String): Long?

    @Throws(EntityNotFoundException::class)
    fun setUserAsAdmin(username: String): Long?

    @Throws(EntityNotFoundException::class)
    fun cancelUserOrder(orderId: Long): Long?


}


@Service
@Transactional
private open class CatalogServiceImpl(
    //val orderController???
    val userRepository: UserRepository
) : CatalogService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var warehouseConnector: WarehouseConnector

    @Autowired
    private lateinit var orderConnector: OrderConnector

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

    //TODO: Rollback?
    override fun addNewOrder(order: String): Long {
        logger.info("Adding a new order...")
        val orderId = 0L //metodo order
        return orderId
    }

    override fun listProducts(): List<StoredProductDto> {
        logger.info("Listing all the products...")
        return warehouseConnector.getProducts()
    }

    override fun getProduct(productId: Long): StoredProductDto? {
        logger.info("Searching for product with id {}", productId)
        val prod = warehouseConnector.getProducts().filter { it.productId == productId }
        return if (prod.isNotEmpty()) prod[0]
        else null
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
        return if(!userInfo.isEmpty) {
            fromEntity(userInfo.get())
        } else null
    }

    override fun updateUserInformation(username: String, email: String, name: String, surname: String, deliveryAddr:String): Long? {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.updateUserInformation(username, email, name, surname, deliveryAddr)
        return if (!user.isEmpty) user.get().getId()
        else null
    }

    @MultiserviceTransactional
    override fun setUserAsAdmin(username: String): Long? {
        logger.info("Setting the role ADMIN to user {}", username)
        val user = userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("username $username") }
        if (user.roles.contains("ADMIN")) {
            logger.info("The user is already an ADMIN!")
        }
        else {
            user.addRoleName("ADMIN")
            logger.info("ADMIN added to the roles of the user")
        }
        return user.getId()
    }

    @Rollback
    private fun rollbackForSetUserAsAdmin(username: String, userId: Long) {
        val user = userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("username $username") }
        user.removeRoleName("ADMIN")
        logger.info("ADMIN added to the roles of the user")
    }

    //TODO: Rollback?
    override fun cancelUserOrder(orderId: Long): Long? {
        logger.info("Cancelling order {}", orderId)
        // metodo orderConnector
        return orderId
    }


}
