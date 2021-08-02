package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.BaseNotGeneratedEntity
import kotlin.math.abs

class LongIdEntity(id: Long? = null) : BaseNotGeneratedEntity<Long>({ id ?: abs(it.nextLong()) })
