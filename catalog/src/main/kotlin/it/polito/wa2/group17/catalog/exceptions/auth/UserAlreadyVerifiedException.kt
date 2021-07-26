package it.polito.wa2.group17.catalog.exceptions.auth

import it.polito.wa2.group17.common.exception.GenericBadRequestException


class UserAlreadyVerifiedException(username: String) :
    GenericBadRequestException("User $username has been already verified")
