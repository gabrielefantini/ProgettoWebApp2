package it.polito.wa2.group17.order.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.mail.MailService
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.order.connector.CatalogConnector
import it.polito.wa2.group17.order.connector.WalletConnector
import it.polito.wa2.group17.order.connector.WarehouseConnector
import it.polito.wa2.group17.order.dto.OrderDto
import it.polito.wa2.group17.order.entities.DeliveryEntity
import it.polito.wa2.group17.order.entities.OrderEntity
import it.polito.wa2.group17.order.entities.OrderStatus
import it.polito.wa2.group17.order.exception.CostNotCorrespondingException
import it.polito.wa2.group17.order.model.OrderRequest
import it.polito.wa2.group17.order.model.ProductModel
import it.polito.wa2.group17.order.model.ProductOrderModel
import it.polito.wa2.group17.order.model.WarehouseModel
import it.polito.wa2.group17.order.repositories.DeliveryRepository
import it.polito.wa2.group17.order.repositories.OrderRepository
import it.polito.wa2.group17.warehouse.exception.NotEnoughMoneyException
import it.polito.wa2.group17.warehouse.exception.NotEnoughProductException
import it.polito.wa2.group17.warehouse.exception.UserNotFoundException
import it.polito.wa2.group17.warehouse.exception.WalletException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface OrderService{
    fun getOrders(): List<OrderDto>
    fun getOrder(orderId: Long): OrderDto
    fun addOrder(orderReq: OrderRequest): OrderDto
    fun updateOrder(orderId: Long,orderReq: OrderRequest): OrderDto
    fun deleteOrder(orderId: Long): OrderDto
}

@Service
class OrderServiceImpl: OrderService {

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

     /*@Autowired
     private lateinit var mailService: MailService*/

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
        val moneyAvailable = walletConnector.getUserWallet(orderReq.userId)?.amount ?: throw WalletException(orderReq.userId)

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
        val orderEntity = OrderEntity(
            buyerId = orderReq.userId,
            price = totalCost,
            status = OrderStatus.ISSUED
        )
        //todo--> vedere se si puo fare anche senza save
        orderRepo.save(orderEntity)
        //aggiungo le delivery list
        orderReq
            .productOrders
            .forEach {
                product ->
                    val warehousesWithProductAvailable = warehouseConnector.getProductWarehouses(product.productId)!!
                    val warehousesWithProductUpdated = warehousesWithProductQuantitiesUpdated(warehousesWithProductAvailable, product.productId)
                    var productQuantity = product.quantity
                    val user = catalogConnector.getUserInfo(orderReq.userId)?: throw UserNotFoundException(orderReq.userId)
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
        //notifico via email
        //TODO
        //post per detrarre il prezzo
        /*
        usersConnector.getAdmins().forEach {
            mailService.sendMessage(it.email, "PRODUCTS QUANTITY ALARM", alertMessage)

        }*/
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
                                        warehouseProduct.quantity -= delivery.quantity.toInt()
                                    }
                        }
                        warehouse
                }
    }

    @Rollback
    private fun rollbackForAddOrder(order: OrderDto){}

    @MultiserviceTransactional
    override fun updateOrder(orderId: Long, orderReq: OrderRequest): OrderDto {
        TODO("Not yet implemented")
    }

    @Rollback
    private fun rollbackForUpdateOrder(){}

    @MultiserviceTransactional
    override fun deleteOrder(orderId: Long): OrderDto {
        TODO("Not yet implemented")
    }

    @Rollback
    private fun rollbackForDeleteOrder(){}
}