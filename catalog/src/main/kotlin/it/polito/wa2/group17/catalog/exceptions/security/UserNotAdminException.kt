package it.polito.wa2.group17.catalog.exceptions.security

import it.polito.wa2.group17.common.exception.AutoLoggableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UserNotAdminException(username: String) :
    AutoLoggableException("User $username is not an admin")
