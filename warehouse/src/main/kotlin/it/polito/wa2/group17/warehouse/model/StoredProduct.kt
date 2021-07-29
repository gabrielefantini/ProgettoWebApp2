package it.polito.wa2.group17.warehouse.model

import it.polito.wa2.group17.common.utils.converter.ConvertibleAlias
import it.polito.wa2.group17.common.utils.converter.CustomConversion
import it.polito.wa2.group17.common.utils.converter.impl.IdAnnotatedExtractor
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class StoredProduct(
    @field:NotNull
    @field:ConvertibleAlias(ConvertibleAlias.From(StoredProductEntity::class, "product"))
    @field:CustomConversion(CustomConversion.Using(IdAnnotatedExtractor::class, StoredProductEntity::class))
    val productId: Long,

    @field:NotNull
    @field:Min(0)
    val quantity: Int,

    @field:NotNull
    @field:Min(0)
    val minimumQuantity: Int
)
