package it.polito.wa2.group17.order.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.order.connector.WalletConnector
import it.polito.wa2.group17.order.connector.WarehouseConnector
import it.polito.wa2.group17.order.dto.OrderDto
import it.polito.wa2.group17.order.exception.CostNotCorrespondingException
import it.polito.wa2.group17.order.model.OrderRequest
import it.polito.wa2.group17.order.repositories.DeliveryRepository
import it.polito.wa2.group17.order.repositories.OrderRepository
import it.polito.wa2.group17.warehouse.exception.MoneyNotEnoughException
import it.polito.wa2.group17.warehouse.exception.WalletException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
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

        if(moneyAvailable < totalCost) throw MoneyNotEnoughException()

        //controllo la disponibilità nei warehouses
        orderReq
            .productOrders
            .forEach {
                product ->
                    val warehousesWithProductAvailable = warehouseConnector.getProductWarehouses(product.productId)
                    warehousesWithProductAvailable
                        ?.forEach {
                            warehouse ->
                                warehouse.productList.forEach {
                                        warehouseProduct ->
                                        if(warehouseProduct.productId == product.productId){
                                            deliveryRepository.findByWarehouseIdAndProductId(warehouse.id, product.productId)
                                                .map {
                                                    delivery -> warehouseProduct.quantity = (warehouseProduct.quantity - delivery.quantity).toInt()
                                                }
                                        }
                                }
                        }

                    val productQuantityAvailableFromWarehouses = warehousesWithProductAvailable
                        ?.reduce { acc, warehouseModel -> acc + warehouseModel. }
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