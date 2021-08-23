package it.polito.wa2.group17.catalog.dto

import it.polito.wa2.group17.common.dto.StoredProductDto
import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection

data class WarehouseDto (
    val id: Long,
    @ConvertibleCollection(StoredProductDto::class)
    val products: List<StoredProductDto>
){}
