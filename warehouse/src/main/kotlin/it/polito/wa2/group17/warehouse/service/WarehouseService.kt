package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import it.polito.wa2.group17.common.mail.MailService
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.warehouse.connector.UsersConnector
import it.polito.wa2.group17.warehouse.dto.*
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import it.polito.wa2.group17.warehouse.entity.WarehouseEntity
import it.polito.wa2.group17.warehouse.exception.ProductNotEnoughException
import it.polito.wa2.group17.warehouse.exception.ProductNotFoundException
import it.polito.wa2.group17.warehouse.model.StoredProduct
import it.polito.wa2.group17.warehouse.model.Warehouse
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import it.polito.wa2.group17.warehouse.repository.StoredProductRepository
import it.polito.wa2.group17.warehouse.repository.WarehouseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


interface WarehouseService {
    fun getWarehouses(): List<Warehouse>
    fun getWarehouse(warehouseId: Long): Warehouse
    fun deleteWarehouse(warehouseId: Long): Warehouse
    fun createWarehouse(products: List<StoredProduct>): Warehouse
    fun sellProductFromAnywhere(sellRequest: SellRequest): SellResponse
    fun sellProduct(warehouseId: Long, sellRequest: SellRequest): SellResponse
    fun fulfillProduct(warehouseId: Long, fulfillRequest: FulfillRequest): StoredProduct
    fun addProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest): StoredProduct
    fun removeProductFromWarehouse(warehouseId: Long, productId: Long): StoredProduct
    fun removeAllProductsFromWarehouse(warehouseId: Long): List<StoredProduct>
    fun updateProductLimit(
        warehouseId: Long,
        productId: Long,
        updateProductRequest: UpdateProductRequest
    ): StoredProduct
}

@Service
private open class WarehouseServiceImpl : WarehouseService {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(WarehouseService::class.java)
    }

    @Autowired
    private lateinit var usersConnector: UsersConnector

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var storedProductRepository: StoredProductRepository

    @Autowired
    private lateinit var mailService: MailService

    @Autowired
    private lateinit var productUpdater: ProductUpdater

    override fun getWarehouses(): List<Warehouse> {
        logger.info("Retrieving all warehouses")
        return warehouseRepository.findAll().map { it.convert() }
    }

    override fun getWarehouse(warehouseId: Long): Warehouse {
        logger.info("Retrieving warehouse with id {}", warehouseId)
        return getWarehouseEntity(warehouseId).convert()
    }

    private fun createWarehouseFromListOfProducts(
        warehouseId: Long? = null,
        products: List<StoredProduct>
    ): WarehouseEntity {

        var warehouseEntity = WarehouseEntity()
        if (warehouseId != null)
            warehouseEntity.setId(warehouseId)

        warehouseEntity = warehouseRepository.save(warehouseEntity)

        val storedProductEntities = products.map {
            storedProductRepository.save(
                StoredProductEntity(
                    product = productRepository.findById(it.productId)
                        .orElseThrow { EntityNotFoundException(it.productId) },
                    quantity = it.quantity,
                    minimumQuantity = it.minimumQuantity,
                    warehouse = warehouseEntity
                )
            )
        }

        warehouseEntity.products.clear()
        warehouseEntity.products.addAll(storedProductEntities)
        warehouseEntity = warehouseRepository.save(warehouseEntity)
        return warehouseEntity
    }

    @MultiserviceTransactional
    override fun deleteWarehouse(warehouseId: Long): Warehouse {
        logger.info("Deleting warehouse with id {}", warehouseId)
        val warehouse =
            getWarehouseEntity(warehouseId)
        storedProductRepository.deleteAllById(warehouse.products.map { it.getId() })
        warehouseRepository.deleteById(warehouseId)
        logger.info("Warehouse with id {} deleted", warehouseId)
        return warehouse.convert()
    }

    @Rollback
    private fun rollbackForDeleteWarehouse(warehouseId: Long, warehouse: Warehouse) {
        logger.warn("Performing rollback of warehouse {} delete.", warehouseId)
        createWarehouseFromListOfProducts(warehouseId, warehouse.products)
        logger.info("Rollback delete of warehouse {} succeed", warehouseId)
    }

    @MultiserviceTransactional
    override fun createWarehouse(products: List<StoredProduct>): Warehouse {
        if (logger.isInfoEnabled) {
            logger.info("Creating warehouse having products ${products.map { it.productId }}")
        }
        val warehouse = createWarehouseFromListOfProducts(products = products)
        logger.info("Warehouse with id {} created", warehouse.getId())
        return warehouse.convert()
    }

    @Rollback
    private fun rollbackForCreateWarehouse(products: List<StoredProduct>, warehouse: Warehouse) {
        logger.warn("Performing rollback of creation of warehouse {}", warehouse.id)
        warehouseRepository.deleteById(warehouse.id)
        logger.warn("Rollback of creation of warehouse {} succeed", warehouse.id)
    }


    @MultiserviceTransactional
    override fun sellProductFromAnywhere(sellRequest: SellRequest): SellResponse {
        val targetProductEntity: StoredProductEntity
        val targetWarehouseEntity: WarehouseEntity


        logger.info(
            "Selling {} products with id {} from any warehouse",
            sellRequest.quantity,
            sellRequest.productID,
        )
        val warehouses = warehouseRepository.findAll()

        synchronized(this) {
            targetWarehouseEntity = warehouses.filter { warehouseEntity ->
                warehouseEntity.products.any {
                    it.product.getId() == sellRequest.productID
                }
            }.apply {
                if (isEmpty())
                    throw ProductNotFoundException(sellRequest.productID)
            }.firstOrNull { warehouse ->
                warehouse.products.any { it.quantity > sellRequest.quantity }
            } ?: throw ProductNotEnoughException(sellRequest.productID, sellRequest.quantity)

            logger.info(
                "Selected warehouse for selling {} products with id {} is {}",
                sellRequest.quantity,
                sellRequest.productID,
                targetWarehouseEntity.getId()
            )

            targetProductEntity =
                targetWarehouseEntity.products.find { it.product.getId() == sellRequest.productID }!!

            targetProductEntity.quantity -= sellRequest.quantity
            warehouseRepository.save(targetWarehouseEntity)
        }

        logger.info(
            "Sold {} products with id {} from {}",
            sellRequest.quantity,
            sellRequest.productID,
            targetWarehouseEntity.getId()
        )

        checkProductLimit(targetProductEntity)

        return SellResponse(sellRequest.productID, sellRequest.quantity, targetProductEntity.getId()!!)
    }

    override fun sellProduct(warehouseId: Long, sellRequest: SellRequest): SellResponse {
        val targetProductEntity: StoredProductEntity
        val targetWarehouseEntity: WarehouseEntity

        logger.info(
            "Selling {} products with id {} from {}",
            sellRequest.quantity,
            sellRequest.productID,
            warehouseId
        )
        synchronized(this) {
            targetWarehouseEntity = warehouseRepository.findById(warehouseId)
                .orElseThrow { EntityNotFoundException(warehouseId) }

            targetProductEntity = targetWarehouseEntity
                .products.firstOrNull { it.product.getId() == sellRequest.productID }
                ?: throw GenericBadRequestException(
                    "Product with id ${sellRequest.productID} cannot be found in ${warehouseId}"
                )

            if (targetProductEntity.quantity < sellRequest.quantity)
                throw ProductNotEnoughException(sellRequest.productID, sellRequest.quantity)

            targetProductEntity.quantity -= sellRequest.quantity
            warehouseRepository.save(targetWarehouseEntity)
        }
        logger.info(
            "Sold {} products with id {} from {}",
            sellRequest.quantity,
            sellRequest.productID,
            targetWarehouseEntity.getId()
        )

        checkProductLimit(targetProductEntity)

        return SellResponse(sellRequest.productID, sellRequest.quantity, targetProductEntity.getId()!!)
    }

    @Rollback
    private fun rollbackForSellProductFromAnywhere(sellRequest: SellRequest, sellResponse: SellResponse) {
        doRollbackForSellProduct(sellResponse)
    }

    @Rollback
    private fun rollbackForSellProduct(warehouseId: Long, sellRequest: SellRequest, sellResponse: SellResponse) {
        doRollbackForSellProduct(sellResponse)
    }

    private fun doRollbackForSellProduct(sellResponse: SellResponse) {
        logger.warn(
            "Performing rollback of selling {} products with id {} from {}",
            sellResponse.quantity,
            sellResponse.productID,
            sellResponse.warehouseID
        )
        val warehouse = warehouseRepository.findById(sellResponse.warehouseID)
            .orElseThrow { EntityNotFoundException(sellResponse.warehouseID) }

        val targetProduct = warehouse.products.firstOrNull { it.product.getId() == sellResponse.productID }
            ?: throw EntityNotFoundException(sellResponse.productID)

        targetProduct.quantity += sellResponse.quantity

        storedProductRepository.save(targetProduct)
        logger.info(
            "Successfully performed rollback of selling {} products with id {} from {}",
            sellResponse.quantity,
            sellResponse.productID,
            sellResponse.warehouseID
        )

    }


    @MultiserviceTransactional
    override fun fulfillProduct(warehouseId: Long, fulfillRequest: FulfillRequest): StoredProduct {
        logger.info(
            "Fulfilling {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            warehouseId
        )
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { EntityNotFoundException(warehouseId) }

        val targetProduct = warehouse.products.firstOrNull { it.product.getId() == fulfillRequest.productID }
            ?: throw EntityNotFoundException(fulfillRequest.productID)

        targetProduct.quantity += fulfillRequest.quantity
        warehouseRepository.save(warehouse)

        logger.info(
            "Fulfilled {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            warehouseId
        )
        return targetProduct.convert()
    }

    @Rollback
    private fun rollbackForFulfillProduct(
        warehouseId: Long,
        fulfillRequest: FulfillRequest,
        storedProduct: StoredProduct
    ) {
        logger.warn(
            "Performing rollback of fulfilling {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            warehouseId
        )

        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { EntityNotFoundException(warehouseId) }

        val targetProduct = warehouse.products.firstOrNull { it.product.getId() == fulfillRequest.productID }
            ?: throw EntityNotFoundException(fulfillRequest.productID)

        synchronized(this) {
            targetProduct.quantity -= fulfillRequest.quantity
            if (targetProduct.quantity < 0)
                logger.warn("WARNING! Performing rollback for fulfill request lead to debit of product ${targetProduct.product.getId()}!!")
            storedProductRepository.save(targetProduct)
        }

        logger.info(
            "Successfully performed rollback of fulfilling {} products with id {} from {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            warehouseId
        )

        checkProductLimit(targetProduct)
    }

    @MultiserviceTransactional
    override fun addProductToWarehouse(warehouseId: Long, addProductRequest: AddProductRequest): StoredProduct {
        val warehouse =
            getWarehouseEntity(warehouseId)

        if (warehouse.products.any { it.product.getId() == addProductRequest.productId })
            throw GenericBadRequestException("Warehouse $warehouseId already contains ${addProductRequest.productId}")


        val product = StoredProductEntity(
            product = productRepository.findById(addProductRequest.productId)
                .orElseThrow { EntityNotFoundException(addProductRequest.productId) },
            quantity = addProductRequest.quantity,
            minimumQuantity = addProductRequest.minimumQuantity,
            warehouse = warehouse
        )
        storedProductRepository.save(product)
        warehouse.products.add(product)
        warehouseRepository.save(warehouse)

        checkProductLimit(product)

        return product.convert()
    }

    @Rollback
    private fun rollbackForAddProductToWarehouse(
        warehouseId: Long,
        addProductRequest: AddProductRequest,
        storedProduct: StoredProduct
    ) {
        val warehouse = getWarehouseEntity(warehouseId)
        val targetProduct = warehouse.products.find { it.product.getId() == storedProduct.productId } ?: return
        storedProductRepository.delete(targetProduct)
    }


    @MultiserviceTransactional
    override fun removeProductFromWarehouse(warehouseId: Long, productId: Long): StoredProduct {
        val warehouse = getWarehouseEntity(warehouseId)
        val product =
            warehouse.products.find { it.product.getId() == productId } ?: throw EntityNotFoundException(productId)
        val result: StoredProduct = product.convert()
        storedProductRepository.delete(product)
        return result
    }

    @Rollback
    private fun rollbackForRemoveProductFromWarehouse(
        warehouseId: Long,
        productId: Long,
        storedProduct: StoredProduct
    ) {
        val warehouse = getWarehouseEntity(warehouseId)
        val product = StoredProductEntity(
            product = productRepository.findById(productId)
                .orElseThrow { EntityNotFoundException(productId) },
            quantity = storedProduct.quantity,
            minimumQuantity = storedProduct.minimumQuantity,
            warehouse = warehouse
        )
        storedProductRepository.save(product)
        warehouse.products.add(product)
        warehouseRepository.save(warehouse)

        checkProductLimit(product)
    }

    @MultiserviceTransactional
    override fun removeAllProductsFromWarehouse(warehouseId: Long): List<StoredProduct> {
        val warehouse = getWarehouseEntity(warehouseId)
        val products = warehouse.products
        storedProductRepository.deleteAll(products)
        return products.map { it.convert() }
    }


    @Rollback
    private fun rollbackForRemoveAllProductsFromWarehouse(warehouseId: Long, products: List<StoredProduct>) {
        val warehouse = getWarehouseEntity(warehouseId)
        val productsEntities = products.map {
            StoredProductEntity(
                product = productRepository.findById(it.productId)
                    .orElseThrow { EntityNotFoundException(it.productId) },
                quantity = it.quantity,
                minimumQuantity = it.minimumQuantity,
                warehouse = warehouse
            )
        }
        storedProductRepository.saveAll(productsEntities)
        warehouse.products.addAll(productsEntities)
        checkProductLimits(warehouse)
    }


    override fun updateProductLimit(
        warehouseId: Long,
        productId: Long,
        updateProductRequest: UpdateProductRequest
    ): StoredProduct =
        productUpdater.updateProduct(warehouseId, productId, updateProductRequest).newState!!.convert()


    private fun checkProductLimits(warehouse: WarehouseEntity) {
        logger.info("Checking product limits for warehouse {}", warehouse.getId())
        val mailBuilder = StringBuilder()
        warehouse.products.forEach {
            if (it.minimumQuantity < it.quantity)
                mailBuilder.append(createAlertMessageForProduct(it))
        }
        sendAlertMessageToAdmins(mailBuilder.toString())
    }

    private fun createAlertMessageForProduct(product: StoredProductEntity) =
        "Product ${product.product.getId()} in warehouse ${product.warehouse.getId()} has as quantity ${product.quantity}. " +
                "Alert threshold is ${product.minimumQuantity}.\n"

    private fun sendAlertMessageToAdmins(alertMessage: String) {
        logger.info("Sending alert to admins: {}", alertMessage)
        usersConnector.getAdmins().forEach {
            mailService.sendMessage(it.email, "PRODUCTS QUANTITY ALARM", alertMessage)
        }
    }

    private fun checkProductLimit(product: StoredProductEntity) {
        if (product.quantity < product.minimumQuantity)
            sendAlertMessageToAdmins(createAlertMessageForProduct(product))
    }

    private fun getWarehouseEntity(warehouseId: Long) =
        warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
}

@Service
private open class ProductUpdater {

    data class ProductUpdated(
        var deltaQuantity: Int = 0,
        var deltaMinimumQuantity: Int = 0,
        var newState: StoredProductEntity? = null
    )

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(WarehouseService::class.java)
    }


    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    private lateinit var storedProductRepository: StoredProductRepository

    @MultiserviceTransactional
    fun updateProduct(warehouseId: Long, productId: Long, updateProductRequest: UpdateProductRequest): ProductUpdated {
        logger.info("Updating product {} from warehouse {}: {}", productId, warehouseId, updateProductRequest)

        val warehouse = warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
        val product =
            warehouse.products.find { it.product.getId() == productId } ?: throw EntityNotFoundException(productId)
        val updateResult = ProductUpdated()

        if (updateProductRequest.quantity != null) {
            updateResult.deltaQuantity = product.quantity - updateProductRequest.quantity
            product.quantity = updateProductRequest.quantity
        }


        if (updateProductRequest.minimumQuantity != null) {
            updateResult.deltaMinimumQuantity = product.minimumQuantity - updateProductRequest.minimumQuantity
            product.minimumQuantity = updateProductRequest.minimumQuantity
        }
        storedProductRepository.save(product)

        updateResult.newState = product
        logger.info("Updated product {} from warehouse {}: {}", productId, warehouseId, updateResult)
        return updateResult
    }

    @Rollback
    private fun rollbackForUpdateProduct(
        warehouseId: Long,
        productId: Long,
        updateProductRequest: UpdateProductRequest,
        updateResult: ProductUpdated
    ) {
        logger.warn(
            "Performing rollback of updating product {} from warehouse {}: {}",
            productId,
            warehouseId,
            updateResult
        )
        val warehouse = warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
        val product =
            warehouse.products.find { it.product.getId() == productId } ?: throw EntityNotFoundException(productId)
        product.quantity += updateResult.deltaQuantity
        product.minimumQuantity += updateResult.deltaMinimumQuantity
        storedProductRepository.save(product)
        logger.info("Rollback for product updating product {} from warehouse {} succeed.", productId, warehouseId)
    }

}
