package it.polito.wa2.group17.order.repositories

import it.polito.wa2.group17.order.entities.ProductOrderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductOrderRepository: CrudRepository<ProductOrderEntity, Long> {
}