package it.polito.wa2.group17.warehouse.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class NotEnoughProductException(productId: Long) :
    Exception("Not enough product with Id: $productId to complete the order")
