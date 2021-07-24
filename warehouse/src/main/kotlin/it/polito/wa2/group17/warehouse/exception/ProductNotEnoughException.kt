package it.polito.wa2.group17.warehouse.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class ProductNotEnoughException(productId: Long, requiredQuantity: Int) :
    Exception("There is no warehouse containing $requiredQuantity products with id $productId")
