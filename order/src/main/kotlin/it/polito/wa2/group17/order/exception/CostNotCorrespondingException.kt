package it.polito.wa2.group17.order.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class CostNotCorrespondingException() :
    Exception("The cost of one or more products have been updated")
