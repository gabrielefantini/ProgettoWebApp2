package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.connector.*
import it.polito.wa2.group17.catalog.security.OnlyAdmins
import it.polito.wa2.group17.common.dto.*
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
    fun getOrders(): List<OrderDto>

    @Throws(EntityNotFoundException::class)
    fun getOrderById(orderId: Long): OrderDto?

    @Throws(EntityNotFoundException::class)
    fun addNewOrder(order: OrderDto): Long

    @Throws(EntityNotFoundException::class)
    fun listProducts(): List<StoredProductDto>

    @Throws(EntityNotFoundException::class)
    fun getProduct(productId: Long): StoredProductDto?

    @Throws(EntityNotFoundException::class)
    fun getWallets(): Wallet?

    @Throws(EntityNotFoundException::class)
    fun cancelUserOrder(orderId: Long)

    @Throws(EntityNotFoundException::class)
    fun getPicture(productId: Long): PostPicture?

    @Throws(EntityNotFoundException::class)
    fun addProductPicture(productId: Long, picture: PostPicture):ProductDto?

    @Throws(EntityNotFoundException::class)
    fun patchProductById(productId: Long, product: PatchProductRequest): ProductDto

    @Throws(EntityNotFoundException::class)
    fun addProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest): StoredProductDto?

    @Throws(EntityNotFoundException::class)
    fun getOrderStatus(orderId: Long): OrderStatus?

    @Throws(EntityNotFoundException::class)
    fun rateProduct(productId: Long, ratingDto: RatingRequest): Long?

    @Throws(EntityNotFoundException::class)
    fun changeProductStatus(productId: Long, status: OrderPatchRequest): Long?

    @Throws(EntityNotFoundException::class)
    fun addWarehouse(warehouseRequest: WarehouseRequest): Long?


    @Throws(EntityNotFoundException::class)
    fun deleteWarehouse(warehouseId: Long): Long

    fun addProduct(productId: Long, putProductRequest: PutProductRequest): Long

    fun deleteProduct(productId: Long): Long


}


@Service
@Transactional
private open class CatalogServiceImpl() : CatalogService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var orderConnector: OrderConnector

    @Autowired
    private lateinit var warehouseConnector: WarehouseConnector

    @Autowired
    private lateinit var signInAndUserInfo: SignInAndUserInfo

    @Autowired
    private lateinit var productConnector: ProductConnector

    override fun getOrders(): List<OrderDto> {
        val username = SecurityContextHolder.getContext().authentication.name
        logger.info("Searching for the orders of the user with username {}", username)
        val user = signInAndUserInfo.findUser(username)
        val userId = user!!.id
        return orderConnector.getOrdersByUsername(userId)!!
    }

    override fun getOrderById(orderId: Long): OrderDto? {
        logger.info("Searching for the order with id {}", orderId)
        return orderConnector.getOrderById(orderId)
    }

    @MultiserviceTransactional
    override fun addNewOrder(order: OrderDto): Long {
        logger.info("Adding a new order...")
        return orderConnector.addOrder(order)
    }

    @Rollback
    private fun rollbackForAddNewOrder(order: OrderDto, id: Long){
        logger.warn("Rollback of order with ID ${order.id}")
    }

    override fun listProducts(): List<StoredProductDto> {
        logger.info("Listing all the products...")
        return warehouseConnector.getProducts()
    }

    override fun getProduct(productId: Long): StoredProductDto? {
        logger.info("Searching for product with id {}", productId)
        return warehouseConnector.getProductById(productId)
    }

    override fun getWallets(): Wallet? {
        val username = SecurityContextHolder.getContext().authentication.name
        logger.info("Searching for the wallets of the user with username {}", username)
        val wallets = warehouseConnector.getWalletsByUsername(username)
        return wallets
    }

    @MultiserviceTransactional
    override fun cancelUserOrder(orderId: Long) {
        logger.info("Cancelling order {}", orderId)
        return orderConnector.cancelOrder(orderId)
    }

    @Rollback
    private fun rollbackForCancelUserOrder(orderId: Long){
        logger.warn("Rollback of cancelling order with ID $orderId")
    }

    override fun getPicture(productId: Long): PostPicture? {
        return warehouseConnector.getProductPicture(productId)
    }

    @MultiserviceTransactional
    @OnlyAdmins
    override fun addProductPicture(productId: Long, picture: PostPicture): ProductDto? {
        return warehouseConnector.setProductPicture(productId, picture)
    }

    @OnlyAdmins
    @MultiserviceTransactional
    override fun patchProductById(productId: Long, product: PatchProductRequest): ProductDto {
        return warehouseConnector.patchProductById(productId, product)!!
    }

    @OnlyAdmins
    @MultiserviceTransactional
    override fun addProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest): StoredProductDto? {
        return warehouseConnector.addProductToWarehouse(warehouseId, addProductRequest)
    }

    override fun getOrderStatus(orderId: Long): OrderStatus? {
        return orderConnector.getStatus(orderId)?.status
    }

    override fun rateProduct(productId: Long, ratingDto: RatingRequest): Long? {
        return productConnector.rateProductById(productId, ratingDto)
    }

    override fun changeProductStatus(productId: Long, status: OrderPatchRequest): Long? {
        return orderConnector.changeStatus(productId, status)
    }

    override fun addWarehouse(warehouseRequest: WarehouseRequest): Long? {
        return warehouseConnector.addWarehouse(warehouseRequest)
    }

    override fun deleteWarehouse(warehouseId: Long): Long {
        return warehouseConnector.deleteWarehouse(warehouseId)
    }

    override fun addProduct(productId: Long, putProductRequest: PutProductRequest): Long {
        return productConnector.addProduct(productId, putProductRequest)
    }

    override fun deleteProduct(productId: Long): Long {
        return productConnector.deleteProduct(productId)
    }

    @Rollback
    private fun rollbackForAddProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest, Dto: StoredProductDto?) {
        logger.info("Rollback for addProductToWarehouse")
    }

    @Rollback
    private fun rollbackForPatchProductById(productId: Long, product: PatchProductRequest, Dto: ProductDto) {
        logger.info("Rollback for patchProductById")
    }

    @Rollback
    private fun rollbackForAddProductPicture(productId: Long, picture: PostPicture, Dto: ProductDto?) {
        logger.info("Rollback for addProductPicture")
    }


}
