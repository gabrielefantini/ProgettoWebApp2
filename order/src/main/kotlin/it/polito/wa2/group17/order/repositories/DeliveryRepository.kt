package it.polito.wa2.group17.order.repositories

import it.polito.wa2.group17.order.entities.DeliveryEntity
import it.polito.wa2.group17.order.entities.OrderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryRepository: CrudRepository<DeliveryEntity,Long> {
    fun findByWarehouseIdAndProductId(warehouseId: Long, productId: Long): List<DeliveryEntity>
}