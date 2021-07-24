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

    }

    @Autowired
    private lateinit var usersConnector: UsersConnector

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    private lateinit var warehouseUpdater: WarehouseUpdater

    @Autowired
    private lateinit var mailService: MailService

    override fun getWarehouses(): List<Warehouse> =
        warehouseRepository.findAll().map { it.convert() }

    override fun getWarehouse(warehouseId: Long): Warehouse =
        warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }.convert()

    @MultiserviceTransactional(DELETE_WAREHOUSE_TRANSACTION_ID)
    override fun deleteWarehouse(warehouseId: Long): Warehouse {
        val warehouse =
            warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
                .convert<Warehouse>()
        warehouseRepository.deleteById(warehouseId)
        return warehouse
    }

    @RollbackFor(DELETE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackDeleteWarehouse(warehouseId: Long, warehouse: Warehouse) {
        val warehouseEntity = WarehouseEntity(products = warehouse.products.map { it.convert() })
        warehouseEntity.setId(warehouseId)
        warehouseRepository.save(warehouseEntity)
    }

    @MultiserviceTransactional(CREATE_WAREHOUSE_TRANSACTION_ID)
    override fun createWarehouse(products: List<StoredProduct>): Warehouse {
        val warehouse = WarehouseEntity(products = products.map { it.convert() })
        return warehouseRepository.save(warehouse).convert()
    }

    @RollbackFor(CREATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackCreateWarehouse(products: List<StoredProduct>, warehouse: Warehouse) {
        warehouseRepository.deleteById(warehouse.id)
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

                targetProductEntity =
                    targetWarehouseEntity.products.find { it.productId == sellRequest.productID }!!

                targetProductEntity.quantity -= sellRequest.quantity
                warehouseRepository.save(targetWarehouseEntity)
            }
        } else {
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
        if (targetProductEntity.quantity < targetProductEntity.minimumQuantity)
            sendAlertMessageToAdmins(createAlertMessageForProduct(targetProductEntity))

        return SellResponse(sellRequest.productID, sellRequest.quantity, targetProductEntity.getId()!!)
    }

    @RollbackFor(SELL_PRODUCT_TRANSACTION_ID)
    private fun rollbackSellProduct(sellRequest: SellRequest, sellResponse: SellResponse) {
        warehouseRepository.findById(sellRequest.warehouseID!!)
            .get().apply {
                products.first { it.productId == sellResponse.productID }.quantity += sellResponse.quantity
                warehouseRepository.save(this)
            }
    }


    override fun fulfillProduct(fulfillRequest: FulfillRequest) {
        //TODO CHECK LIMIT
        TODO("Not yet implemented")
    }

    private fun checkProductLimits(warehouse: WarehouseEntity) {
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

    private fun sendAlertMessageToAdmins(alertMessage: String) = usersConnector.getAdmins().forEach {
        mailService.sendMessage(it.email, "PRODUCTS QUANTITY ALARM", alertMessage)
    }
}

@Service
private class WarehouseUpdater() {
    private companion object {
        private const val PARTIALLY_UPDATE_WAREHOUSE_TRANSACTION_ID = "partiallyUpdateWarehouse"
        private const val UPDATE_WAREHOUSE_TRANSACTION_ID = "updateWarehouse"
    }

    @Autowired
    private lateinit var warehouseRepository: WarehouseRepository

    @MultiserviceTransactional(PARTIALLY_UPDATE_WAREHOUSE_TRANSACTION_ID)
    fun partiallyUpdateWarehouse(warehouseId: Long, products: List<StoredProduct>): WarehouseUpdated {
        val warehouse =
            warehouseRepository.findById(warehouseId).orElseThrow { EntityNotFoundException(warehouseId) }
        val oldVersion = warehouse.convert<WarehouseEntity>() //just a clone

        warehouse.products = warehouse.products.map {
            val matching = products.filter { p -> it.productId == p.productId }
            return@map when {
                matching.size > 1 -> {
                    throw GenericBadRequestException("Multiple data for product id ${it.productId}")
                }
                matching.isEmpty() -> it
                else -> {
                    it.apply {
                        quantity = matching[0].quantity
                        minimumQuantity = matching[0].minimumQuantity
                    }
                }
            }
        }

        val newVersion = warehouseRepository.save(warehouse)
        return WarehouseUpdated(previousVersion = oldVersion, newVersion = newVersion)
    }

    @RollbackFor(UPDATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackPartiallyUpdateWarehouse(
        warehouseId: Long,
        products: List<StoredProduct>,
        warehouseUpdated: WarehouseUpdated
    ) = warehouseRepository.save(warehouseUpdated.previousVersion!!)

    fun updateOrInsertWarehouse(warehouseId: Long, products: List<StoredProduct>): WarehouseUpdated {
        val warehouseOptional =
            warehouseRepository.findById(warehouseId)
        return if (warehouseOptional.isEmpty) {
            WarehouseUpdated(
                previousVersion = null,
                newVersion = warehouseRepository.save(WarehouseEntity(products = products.map { it.convert() }))
            )
        } else {
            val warehouse = warehouseOptional.get()
            val oldVersion: WarehouseEntity = warehouse.convert()
            warehouse.products = products.map { it.convert() }
            val newVersion = warehouseRepository.save(warehouse)
            WarehouseUpdated(previousVersion = oldVersion, newVersion = newVersion)
        }
    }

    @RollbackFor(UPDATE_WAREHOUSE_TRANSACTION_ID)
    private fun rollbackUpdateOrInsertWarehouse(
        warehouseId: Long,
        products: List<StoredProduct>,
        warehouseUpdated: WarehouseUpdated
    ) {
        if (warehouseUpdated.previousVersion == null) {
            warehouseRepository.deleteById(warehouseId)
        } else warehouseRepository.save(warehouseUpdated.previousVersion)
    }
}
