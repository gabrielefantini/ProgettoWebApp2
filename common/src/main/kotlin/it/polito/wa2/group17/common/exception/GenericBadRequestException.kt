package it.polito.wa2.group17.common.exception

import it.polito.wa2.group17.common.exception.AutoLoggableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
open class GenericBadRequestException(message: String) : AutoLoggableException(message)
