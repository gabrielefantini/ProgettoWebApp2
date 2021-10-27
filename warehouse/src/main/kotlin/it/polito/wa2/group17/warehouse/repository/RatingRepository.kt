package it.polito.wa2.group17.warehouse.repository

import it.polito.wa2.group17.warehouse.entity.ProductEntity
import it.polito.wa2.group17.warehouse.entity.RatingEntity
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<RatingEntity, Long> {
}
