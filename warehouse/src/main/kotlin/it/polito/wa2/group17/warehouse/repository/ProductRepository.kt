package it.polito.wa2.group17.warehouse.repository

import it.polito.wa2.group17.warehouse.entity.ProductEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository: CrudRepository<ProductEntity,Long>{
    fun findProductEntitiesByCategory(category: String?): List<ProductEntity>
    fun save(product: Optional<ProductEntity>)
}
