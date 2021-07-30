package it.polito.wa2.group17.order.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.mail.MailService
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.order.dto.OrderDto
import it.polito.wa2.group17.order.model.OrderRequest
import it.polito.wa2.group17.order.repositories.OrderRepository
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
    //      -> per ogni warehouse che ha il prodotto, controllo la quantità disponibile e la inserisco in una lista
    //      -> per ogni elemento della lista, controllo se ci sia un delivery pendente e in caso sottraggo quella quantità alla lista
    //      -> inserisco nella delivery list la quantità di prodotto richiesta
    // 4. viene detratto il prezzo dal wallet -> POST wallet-service/wallets/{walletID}/transactions
    //      e sia utente che admin vanno notificati via email
    @MultiserviceTransactional
    override fun addOrder(orderReq: OrderRequest): OrderDto {
        TODO("Not yet implemented")
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