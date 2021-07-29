package it.polito.wa2.group17.warehouse.repository

import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import it.polito.wa2.group17.warehouse.entity.WarehouseEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StoredProductRepository : CrudRepository<StoredProductEntity, Long>
