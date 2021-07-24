package it.polito.wa2.group17.warehouse.model

import it.polito.wa2.group17.warehouse.entity.WarehouseEntity

data class WarehouseUpdated(val previousVersion: WarehouseEntity?, val newVersion: WarehouseEntity)
