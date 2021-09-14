package it.polito.wa2.group17.order.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import it.polito.wa2.group17.common.mail.MailService
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.order.connector.CatalogConnector
import it.polito.wa2.group17.order.connector.WalletConnector
import it.polito.wa2.group17.order.connector.WarehouseConnector
import it.polito.wa2.group17.order.dto.OrderDto
import it.polito.wa2.group17.order.dto.OrderUpdate
import it.polito.wa2.group17.order.entities.DeliveryEntity
import it.polito.wa2.group17.order.entities.OrderEntity
import it.polito.wa2.group17.order.entities.OrderStatus
import it.polito.wa2.group17.order.exception.CostNotCorrespondingException
import it.polito.wa2.group17.order.model.*
import it.polito.wa2.group17.order.repositories.DeliveryRepository
import it.polito.wa2.group17.order.repositories.OrderRepository
import it.polito.wa2.group17.warehouse.exception.*
import org.hibernate.criterion.Order
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface OrderService{
    fun getOrders(): List<OrderDto>
    fun getOrder(orderId: Long): OrderDto
    fun addOrder(orderReq: OrderRequest): OrderDto
    fun updateOrder(orderId: Long,orderPatchRequest: OrderPatchRequest): OrderUpdate
    fun deleteOrder(orderId: Long): OrderDto
}

@Service
class OrderServiceImpl: OrderService {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OrderService::class.java)
    }

    @Autowired
    lateinit var orderRepo: OrderRepository

    @Autowired
    lateinit var deliveryRepository: DeliveryRepository

     @Autowired
     lateinit var warehouseConnector: WarehouseConnector

     @Autowired
     lateinit var walletConnector: WalletConnector

     @Autowired
     lateinit var catalogConnector: CatalogConnector

     @Autowired
     private lateinit var mailService: MailService

    //rollback not needed
    override fun getOrders(): List<OrderDto> {
        return orderRepo.findAll().map { it.convert() }
    }

    override fun getOrder(orderId: Long): OrderDto {
        return orderRepo.findById(orderId).orElseThrow{ EntityNotFoundException(orderId) }.convert()
    }

    //rollback needed

    // 1. calcolo del prezzo totale estrando i prezzi da ogni singolo prodotto nell'ordine -> GET warehouse-service/products/{productID}
    // 2. controllo del wallet del cliente per verificare che abbia abbastanza denaro per l'acquisto -> GET wallet-service/wallets/{userID}
    // 3. controllo la disponibilità dei prodotti nei warehouses -> GET warehouse-service/products/{productID}/warehouses
    //      -> per ogni warehouse che ha il prodotto, controllo la quantità disponibile (GET warehouse-service/warehouse/{warehouseId}/products/{productId}) e la INSERISCO in una lista
    //      -> per ogni elemento della lista, controllo se ci sia un delivery pendente e in caso SOTTRAGGO quella quantità alla lista
    //      -> INSERISCO nella delivery list la quantità di prodotto richiesta
    // 4. viene detratto il prezzo dal wallet -> POST wallet-service/wallets/{walletID}/transactions
    //      e sia utente che admin vanno notificati via email

    @MultiserviceTransactional
    override fun addOrder(orderReq: OrderRequest): OrderDto {
        logger.info("Adding Order")
        //calcolo del costo totale
        val totalCost =
        orderReq
            .productOrders
            .map {
                product -> warehouseConnector.getProduct(product.productId)?.price ?: 0.0
            }.reduce { acc, productPrice -> productPrice + acc}

        val totalEstimedCost =
        orderReq
            .productOrders
            .map{ product -> product.price }
            .reduce{ acc, price -> price + acc }

        if(totalCost != totalEstimedCost) throw CostNotCorrespondingException()

        //controllo che il cliente non sia un poraccio
        val wallet = walletConnector.getUserWallet(orderReq.userId) ?: throw WalletException(orderReq.userId)
        val moneyAvailable = wallet.amount

        if(moneyAvailable < totalCost) throw NotEnoughMoneyException()

        //controllo la disponibilità nei warehouses
        orderReq
            .productOrders
            .forEach {
                product ->
                val warehousesWithProductAvailable =
                    warehouseConnector.getProductWarehouses(product.productId)
                    ?: throw NotEnoughProductException(product.productId)
                val warehousesWithProductUpdated = warehousesWithProductQuantitiesUpdated(warehousesWithProductAvailable, product.productId)
                if(!checkIfProductIsAvailable(warehousesWithProductUpdated, product)) throw NotEnoughProductException(product.productId)


            }
        //creo oggetto order
        var orderEntity = OrderEntity(
            buyerId = orderReq.userId,
            price = totalCost,
            status = OrderStatus.ISSUED
        )
        //orderReq.productOrders.forEach { it -> orderEntity.productOrders?.add(it.convert()) }

        //todo--> vedere se si puo fare anche senza save
        orderEntity = orderRepo.save(orderEntity)

        val user = catalogConnector.getUserInfo(orderReq.userId)?: throw UserNotFoundException(orderReq.userId)
        
        //aggiungo le delivery list
        orderReq
            .productOrders
            .forEach {
                product ->
                    val warehousesWithProductAvailable = warehouseConnector.getProductWarehouses(product.productId)!!
                    val warehousesWithProductUpdated = warehousesWithProductQuantitiesUpdated(warehousesWithProductAvailable, product.productId)
                    var productQuantity = product.quantity
                    warehousesWithProductUpdated
                        .forEach {
                                warehouse ->
                            if(productQuantity > 0){
                                val amountAvailable = warehouse.productList.find { it.productId == product.productId }?.quantity ?: 0
                                if(amountAvailable - productQuantity >= 0){
                                    val delivery = DeliveryEntity(
                                        user.deliveryAddr,
                                        warehouse.id,
                                        product.productId,
                                        productQuantity,
                                        orderEntity
                                    )
                                    productQuantity = 0
                                    deliveryRepository.save(delivery)
                                } else {
                                    val delivery = DeliveryEntity(
                                        user.deliveryAddr,
                                        warehouse.id,
                                        product.productId,
                                        amountAvailable.toLong(),
                                        orderEntity
                                    )
                                    productQuantity -= amountAvailable
                                    deliveryRepository.save(delivery)
                                }
                            }
                        }
            }
        //detraggo il denaro dal wallet dell'utente
        val transaction = TransactionModel(
            null,
            "Payment of order number ${orderEntity.getId()}",
            -totalCost,
            orderReq.userId,
            Calendar.getInstance().toInstant()
        )

        walletConnector.addWalletTransaction(transaction, wallet.walletId) ?: throw TransactionException()
        
        //TODO
        //notifico via email
        /*
        mailService.sendMessage(user.email, "Order number ${orderEntity.getId()}", "")

        usersConnector.getAdmins().forEach {
            mailService.sendMessage(it.email, "PRODUCTS QUANTITY ALARM", "")

        }
        */
        return orderEntity.convert()
        
    }

    private fun checkIfProductIsAvailable(warehouses: List<WarehouseModel>, product: ProductOrderModel): Boolean {
        val totalProductQuantityAvailable =
            warehouses
                .map{
                    warehouse ->
                        warehouse.productList
                            .map {
                                warehouseProduct ->
                                    if(warehouseProduct.productId == product.productId){
                                        warehouseProduct.quantity
                                    } else  0
                            }
                            .reduce{ acc, it -> acc + it}
                }
                .reduce {
                    acc,
                    productQuantity -> acc + productQuantity
                }

        if(totalProductQuantityAvailable < product.quantity)    return false
        return true
    }

    private fun warehousesWithProductQuantitiesUpdated(warehouses: List<WarehouseModel>, productId: Long) : List<WarehouseModel> {
            return  warehouses
                .map {
                        warehouse ->
                        warehouse.productList.forEach { warehouseProduct ->
                            if (warehouseProduct.productId == productId)
                                deliveryRepository.findByWarehouseIdAndProductId(warehouse.id, productId)
                                    .forEach { delivery ->
                                        //fai la sottrazione solo se gli ordini non sono ancora partiti
                                        if (delivery.order.status == OrderStatus.ISSUED)
                                        warehouseProduct.quantity -= delivery.quantity.toInt()
                                    }
                        }
                        warehouse
                }
    }

    @Rollback
    private fun rollbackForAddOrder(orderReq: OrderRequest,order: OrderDto){
        logger.warn("rollback of order with ID ${order.id}")
        orderRepo.deleteById(order.id)
    }

    @MultiserviceTransactional
    override fun updateOrder(orderId: Long, orderPatchRequest: OrderPatchRequest): OrderUpdate {
        logger.info("Updating order status")

        val order = orderRepo.findByIdOrNull(orderId) ?: throw EntityNotFoundException(orderId)

        when (order.status){
            OrderStatus.ISSUED -> {
                if(orderPatchRequest.status == OrderStatus.DELIVERING){
                    order.deliveryList?.forEach {
                        deliveryEntity ->
                            warehouseConnector
                                .buyProduct(
                                    deliveryEntity.warehouseId,
                                    ProductBuyRequest(deliveryEntity.productId, deliveryEntity.quantity.toInt())
                                )
                    }
                    order.status = OrderStatus.DELIVERING
                    return OrderUpdate(orderRepo.save(order).convert(), OrderStatus.ISSUED)
                }
                else throw GenericBadRequestException("Invalid status update")
            }

            OrderStatus.DELIVERING -> {
                if( orderPatchRequest.status == OrderStatus.DELIVERED){
                    order.status = OrderStatus.DELIVERED
                    return OrderUpdate(orderRepo.save(order).convert(), OrderStatus.DELIVERING)
                }
                if( orderPatchRequest.status == OrderStatus.FAILED){
                    restoreQuantities(order)
                    refundCustomer(order)
                    order.status = OrderStatus.FAILED
                    return OrderUpdate(orderRepo.save(order).convert(), OrderStatus.DELIVERING)
                } else throw GenericBadRequestException("Invalid status update")
            }

            OrderStatus.DELIVERED -> {
                if( orderPatchRequest.status == OrderStatus.FAILED){
                    restoreQuantities(order)
                    refundCustomer(order)
                    order.status = OrderStatus.FAILED
                    return OrderUpdate(orderRepo.save(order).convert(), OrderStatus.DELIVERED)
                } else throw GenericBadRequestException("Invalid status update")
            }
            else -> throw GenericBadRequestException("Invalid status update")
        }
    }

    private fun restoreQuantities(order: OrderEntity){
        //restore quantity for each warehouse
        order.deliveryList
            ?.forEach {
            delivery ->
                warehouseConnector
                    .updateProductQuantity(
                        delivery.warehouseId,
                        delivery.productId,
                        UpdateProductRequest(delivery.quantity.toInt())
                    )
        }
    }
    private fun refundCustomer(order: OrderEntity){
        val wallet = walletConnector.getUserWallet(order.buyerId) ?: throw WalletException(order.buyerId)
        walletConnector
            .addWalletTransaction(
                TransactionModel(
                    reason = "refund of order with ID ${order.getId()}",
                    userId = order.buyerId,
                    amount = order.price,
                    timeInstant = Calendar.getInstance().toInstant(),
                ),
                wallet.walletId
            )
    }

    @Rollback
    private fun rollbackForUpdateOrder(orderId: Long, orderPatchRequest: OrderPatchRequest, orderUpdate: OrderUpdate){
        logger.warn("Rollback of status update")
        val orderEntity = orderRepo.findByIdOrNull(orderUpdate.newOrder.id) ?: throw EntityNotFoundException(orderUpdate.newOrder.id)
        orderEntity.status = orderUpdate.oldStatus
        orderRepo.save(orderEntity)
    }

    @MultiserviceTransactional
    override fun deleteOrder(orderId: Long): OrderDto {
        val order = orderRepo.findByIdOrNull(orderId) ?: throw EntityNotFoundException(orderId)
        if(order.status != OrderStatus.ISSUED) throw GenericBadRequestException("Too late to delete the order")
        refundCustomer(order)
        order.status = OrderStatus.CANCELED
        return orderRepo.save(order).convert()
    }

    @Rollback
    private fun rollbackForDeleteOrder(orderId: Long, order: OrderDto ){
        val order = orderRepo.findByIdOrNull(orderId) ?: throw EntityNotFoundException(orderId)
        order.status = OrderStatus.ISSUED
        orderRepo.save(order)
    }
}