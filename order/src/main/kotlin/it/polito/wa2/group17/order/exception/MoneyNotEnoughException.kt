package it.polito.wa2.group17.warehouse.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class MoneyNotEnoughException() :
    Exception("Not enough money in the user's wallet to complete the order")
