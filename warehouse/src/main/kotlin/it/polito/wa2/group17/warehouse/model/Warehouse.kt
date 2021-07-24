package it.polito.wa2.group17.warehouse.model

import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection

data class Warehouse(
    val id: Long,
    @ConvertibleCollection(StoredProduct::class)
    val products: List<StoredProduct>
)
