package it.polito.wa2.group17.warehouse.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ProductNotFoundException(productId: Long) : Exception("Product with id $productId cannot be found")
