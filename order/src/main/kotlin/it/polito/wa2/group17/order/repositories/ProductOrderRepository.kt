package it.polito.wa2.group17.order.repositories

import it.polito.wa2.group17.order.entities.OrderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductOrderRepository: CrudRepository<OrderEntity,Long> {
}