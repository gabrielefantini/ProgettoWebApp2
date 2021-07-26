package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import it.polito.wa2.group17.common.mail.MailService
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.RollbackFor
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.warehouse.connector.UsersConnector
import it.polito.wa2.group17.warehouse.dto.FulfillRequest
import it.polito.wa2.group17.warehouse.dto.SellRequest
import it.polito.wa2.group17.warehouse.dto.SellResponse
import it.polito.wa2.group17.warehouse.entity.WarehouseEntity
import it.polito.wa2.group17.warehouse.exception.ProductNotEnoughException
import it.polito.wa2.group17.warehouse.exception.ProductNotFoundException
import it.polito.wa2.group17.warehouse.model.StoredProduct
import it.polito.wa2.group17.warehouse.model.Warehouse
import it.polito.wa2.group17.warehouse.model.WarehouseUpdated
import it.polito.wa2.group17.warehouse.repository.WarehouseRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity as StoredProductEntity1


interface WarehouseService {
    fun getWarehouses(): List<Warehouse>
    fun getWarehouse(warehouseId: Long): Warehouse
    fun deleteWarehouse(warehouseId: Long): Warehouse
    fun createWarehouse(products: List<StoredProduct>): Warehouse
    fun partiallyUpdateWarehouse(warehouseId: Long, products: List<StoredProduct>): Warehouse
    fun updateOrInsertWarehouse(warehouseId: Long, products: List<StoredProduct>): Warehouse
    fun sellProduct(sellRequest: SellRequest): SellResponse
    fun fulfillProduct(fulfillRequest: FulfillRequest)
}

@Service
private class WarehouseServiceImpl : WarehouseService {

    private companion object {
        private const val DELETE_WAREHOUSE_TRANSACTION_ID = "deleteWarehouse"
        private const val CREATE_WAREHOUSE_TRANSACTION_ID = "createWarehouse"
        private const val SELL_PRODUCT_TRANSACTION_ID = "sellProduct"
        private const val FULFILL_PRODUCT_TRANSACTION_ID = "fulfillProduct"
        private val logger: Logger = LoggerFactory.getLogger(WarehouseService::class.java)
    }

    @Autowired
    private lateinit var usersConnector: UsersConnector

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    private lateinit var warehouseUpdater: WarehouseUpdater

    @Autowired
    private lateinit var mailService: MailService

    override fun getWarehouses(): List<Warehouse> {
        logger.info("Retrieving all warehouses")
        return warehouseRepository.findAll().map { it.convert() }
    }

    override fun getWarehouse(warehouseId: Long): Warehouse {
        logger.info("Retrieving warehouse with id {}", warehouseId)
        return warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }.convert()
    }

    @MultiserviceTransactional(DELETE_WAREHOUSE_TRANSACTION_ID)
    override fun deleteWarehouse(warehouseId: Long): Warehouse {
        logger.info("Deleting warehouse with id {}", warehouseId)
        val warehouse =
            warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
                .convert<Warehouse>()
        warehouseRepository.deleteById(warehouseId)
        logger.info("Warehouse with id {} deleted", warehouseId)
        return warehouse
    }

    @RollbackFor(DELETE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackDeleteWarehouse(warehouseId: Long, warehouse: Warehouse) {
        logger.warn("Performing rollback of warehouse {} delete.", warehouseId)
        val warehouseEntity =
            WarehouseEntity(products = warehouse.products.map { it.convert<StoredProductEntity1>() }.toMutableList())
        warehouseEntity.setId(warehouseId)
        warehouseRepository.save(warehouseEntity)
        logger.info("Rollback delete of warehouse {} succeed", warehouseId)
    }

    @MultiserviceTransactional(CREATE_WAREHOUSE_TRANSACTION_ID)
    override fun createWarehouse(products: List<StoredProduct>): Warehouse {
        if (logger.isInfoEnabled) {
            logger.info("Creating warehouse having products ${products.map { it.productId }}")
        }
        val warehouse = WarehouseEntity(products = products.map { it.convert<StoredProductEntity1>() }.toMutableList())
        return warehouseRepository.save(warehouse)
            .let { logger.info("Warehouse with id {} created", it.getId()) }
            .convert()
    }

    @RollbackFor(CREATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackCreateWarehouse(products: List<StoredProduct>, warehouse: Warehouse) {
        logger.warn("Performing rollback of creation of warehouse {}", warehouse.id)
        warehouseRepository.deleteById(warehouse.id)
        logger.warn("Rollback of creation of warehouse {} succeed", warehouse.id)
    }


    override fun partiallyUpdateWarehouse(warehouseId: Long, products: List<StoredProduct>): Warehouse {
        val result = warehouseUpdater.partiallyUpdateWarehouse(warehouseId, products).newVersion
        checkProductLimits(result)
        return result.convert()
    }

    override fun updateOrInsertWarehouse(warehouseId: Long, products: List<StoredProduct>): Warehouse {
        val result = warehouseUpdater.updateOrInsertWarehouse(warehouseId, products).newVersion
        checkProductLimits(result)
        return result.convert()
    }


    @MultiserviceTransactional(SELL_PRODUCT_TRANSACTION_ID)
    override fun sellProduct(sellRequest: SellRequest): SellResponse {
        val targetProductEntity: StoredProductEntity1
        val targetWarehouseEntity: WarehouseEntity

        if (sellRequest.warehouseID == null) {
            logger.info(
                "Selling {} products with id {} from any warehouse",
                sellRequest.quantity,
                sellRequest.productID,
            )
            val warehouses = warehouseRepository.findAll()

            synchronized(this) {
                targetWarehouseEntity = warehouses.filter { warehouseEntity ->
                    warehouseEntity.products.any {
                        it.productId == sellRequest.productID
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
                    targetWarehouseEntity.products.find { it.productId == sellRequest.productID }!!

                targetProductEntity.quantity -= sellRequest.quantity
                warehouseRepository.save(targetWarehouseEntity)
            }
        } else {
            logger.info(
                "Selling {} products with id {} from {}",
                sellRequest.quantity,
                sellRequest.productID,
                sellRequest.warehouseID
            )
            synchronized(this) {
                targetWarehouseEntity = warehouseRepository.findById(sellRequest.warehouseID!!)
                    .orElseThrow { EntityNotFoundException(sellRequest.warehouseID!!) }

                targetProductEntity = targetWarehouseEntity
                    .products.firstOrNull { it.productId == sellRequest.productID } ?: throw GenericBadRequestException(
                    "Product with id ${sellRequest.productID} cannot be found in ${sellRequest.warehouseID}"
                )

                if (targetProductEntity.quantity < sellRequest.quantity)
                    throw ProductNotEnoughException(sellRequest.productID, sellRequest.quantity)

                targetProductEntity.quantity -= sellRequest.quantity
                warehouseRepository.save(targetWarehouseEntity)
            }

        }
        logger.info(
            "Sold {} products with id {} from {}",
            sellRequest.quantity,
            sellRequest.productID,
            sellRequest.warehouseID
        )

        if (targetProductEntity.quantity < targetProductEntity.minimumQuantity)
            sendAlertMessageToAdmins(createAlertMessageForProduct(targetProductEntity))

        return SellResponse(sellRequest.productID, sellRequest.quantity, targetProductEntity.getId()!!)
    }

    @RollbackFor(SELL_PRODUCT_TRANSACTION_ID)
    private fun rollbackSellProduct(sellRequest: SellRequest, sellResponse: SellResponse) {
        logger.warn(
            "Performing rollback of selling {} products with id {} from {}",
            sellResponse.quantity,
            sellResponse.productID,
            sellResponse.warehouseID
        )
        warehouseRepository.findById(sellResponse.warehouseID)
            .orElseThrow { EntityNotFoundException(sellResponse.warehouseID) }
            .apply {
                products.first { it.productId == sellResponse.productID }.quantity += sellResponse.quantity
                warehouseRepository.save(this)
            }
        logger.info(
            "Successfully performed rollback of selling {} products with id {} from {}",
            sellResponse.quantity,
            sellResponse.productID,
            sellResponse.warehouseID
        )
    }


    @MultiserviceTransactional(FULFILL_PRODUCT_TRANSACTION_ID)
    override fun fulfillProduct(fulfillRequest: FulfillRequest) {
        logger.info(
            "Fulfilling {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            fulfillRequest.warehouseID
        )
        val warehouse = warehouseRepository.findById(fulfillRequest.warehouseID)
            .orElseThrow { EntityNotFoundException(fulfillRequest.warehouseID) }

        val targetProduct = warehouse.products.firstOrNull { it.productId == fulfillRequest.productID }
            ?: throw EntityNotFoundException(fulfillRequest.productID)

        targetProduct.quantity += fulfillRequest.quantity
        warehouseRepository.save(warehouse)

        logger.info(
            "Fulfilled {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            fulfillRequest.warehouseID
        )
    }

    @RollbackFor(FULFILL_PRODUCT_TRANSACTION_ID)
    private fun rollbackFulfillProduct(fulfillRequest: FulfillRequest) {
        logger.warn(
            "Performing rollback of fulfilling {} products with id {} to {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            fulfillRequest.warehouseID
        )

        val warehouse = warehouseRepository.findById(fulfillRequest.warehouseID)
            .orElseThrow { EntityNotFoundException(fulfillRequest.warehouseID) }

        val targetProduct = warehouse.products.firstOrNull { it.productId == fulfillRequest.productID }
            ?: throw EntityNotFoundException(fulfillRequest.productID)

        synchronized(this) {
            targetProduct.quantity -= fulfillRequest.quantity
            if (targetProduct.quantity < 0)
                logger.warn("WARNING! Performing rollback for fulfill request lead to debit of product ${targetProduct.productId}!!")
            warehouseRepository.save(warehouse)
        }

        logger.info(
            "Successfully performed rollback of fulfilling {} products with id {} from {}",
            fulfillRequest.quantity,
            fulfillRequest.productID,
            fulfillRequest.warehouseID
        )

        if (targetProduct.quantity < targetProduct.minimumQuantity)
            sendAlertMessageToAdmins(createAlertMessageForProduct(targetProduct))
    }

    private fun checkProductLimits(warehouse: WarehouseEntity) {
        logger.info("Checking product limits for warehouse {}", warehouse.getId())
        val mailBuilder = StringBuilder()
        warehouse.products.forEach {
            if (it.minimumQuantity < it.quantity)
                mailBuilder.append(createAlertMessageForProduct(it))
        }
        sendAlertMessageToAdmins(mailBuilder.toString())
    }

    private fun createAlertMessageForProduct(product: StoredProductEntity1) =
        "Product ${product.productId} in warehouse ${product.warehouse.getId()} has as quantity ${product.quantity}." +
                "Alert threshold is ${product.minimumQuantity}.\n"

    private fun sendAlertMessageToAdmins(alertMessage: String) {
        logger.info("Sending alert {} to admins", alertMessage)
        usersConnector.getAdmins().forEach {
            mailService.sendMessage(it.email, "PRODUCTS QUANTITY ALARM", alertMessage)
        }
    }
}

@Service
private class WarehouseUpdater() {
    private companion object {
        private const val PARTIALLY_UPDATE_WAREHOUSE_TRANSACTION_ID = "partiallyUpdateWarehouse"
        private const val UPDATE_WAREHOUSE_TRANSACTION_ID = "updateWarehouse"
        private val logger: Logger = LoggerFactory.getLogger(WarehouseService::class.java)
    }

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @MultiserviceTransactional(PARTIALLY_UPDATE_WAREHOUSE_TRANSACTION_ID)
    fun partiallyUpdateWarehouse(warehouseId: Long, products: List<StoredProduct>): WarehouseUpdated {
        logger.info(
            "Partially updating warehouse {} with products {}", warehouseId, products
        )


        val warehouse =
            warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
        val oldVersion = warehouse.convert<WarehouseEntity>() //just a clone


        products.forEach {
            val matching = warehouse.products.firstOrNull { p -> it.productId == p.productId }
            if (matching == null) {
                warehouse.products.add(
                    StoredProductEntity1(
                        productId = it.productId,
                        quantity = it.quantity,
                        minimumQuantity = it.minimumQuantity,
                        warehouse = warehouse
                    )
                )
            } else {
                matching.apply {
                    quantity = it.quantity
                    minimumQuantity = it.minimumQuantity
                }
            }
        }
        val newVersion = warehouseRepository.save(warehouse)
        logger.info("Warehouse {} updated", warehouseId)
        return WarehouseUpdated(previousVersion = oldVersion, newVersion = newVersion)
    }

    @RollbackFor(UPDATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackPartiallyUpdateWarehouse(
        warehouseId: Long,
        products: List<StoredProduct>,
        warehouseUpdated: WarehouseUpdated
    ) {
        logger.warn("Performing rollback of warehouse {} partially update {}", warehouseId, products)
        warehouseRepository.save(warehouseUpdated.previousVersion!!)
        logger.warn("Rollback of warehouse {} partially update performed successfully", warehouseId)
    }

    fun updateOrInsertWarehouse(warehouseId: Long, products: List<StoredProduct>): WarehouseUpdated {
        val warehouseOptional =
            warehouseRepository.findById(warehouseId)

        return if (warehouseOptional.isEmpty) {
            logger.info("Saving warehouse {} with products {}", warehouseId, products)
            val warehouse =
                WarehouseEntity(products = products.map { it.convert<StoredProductEntity1>() }.toMutableList())
            warehouse.setId(warehouseId)
            WarehouseUpdated(
                previousVersion = null,
                newVersion = warehouseRepository.save(warehouse)
            ).apply { logger.info("Saved warehouse {}", warehouseId) }
        } else {
            logger.info("Fully updating warehouse {} with products {}", warehouseId, products)
            val warehouse = warehouseOptional.get()
            val oldVersion: WarehouseEntity = warehouse.convert()
            warehouse.products = products.map { it.convert<StoredProductEntity1>() }.toMutableList()
            val newVersion = warehouseRepository.save(warehouse)
            logger.info("Warehouse {} updated", warehouseId)
            WarehouseUpdated(previousVersion = oldVersion, newVersion = newVersion)
        }
    }

    @RollbackFor(UPDATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackUpdateOrInsertWarehouse(
        warehouseId: Long,
        products: List<StoredProduct>,
        warehouseUpdated: WarehouseUpdated
    ) {
        logger.warn("Performing rollback of update/insert of {}", warehouseId)
        if (warehouseUpdated.previousVersion == null) {
            warehouseRepository.deleteById(warehouseId).let { logger.info("Deleted warehouse {}", warehouseId) }
        } else warehouseRepository.save(warehouseUpdated.previousVersion)
            .let { logger.info("Restored warehouse {}", warehouseId) }
    }
}
