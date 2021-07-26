package it.polito.wa2.group17.catalog.exceptions.auth

import it.polito.wa2.group17.common.exception.AutoLoggableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class UserAlreadyPresentException(username: String) :
    AutoLoggableException("User $username is already present")
