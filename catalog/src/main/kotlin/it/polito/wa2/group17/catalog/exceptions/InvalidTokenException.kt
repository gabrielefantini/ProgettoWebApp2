package it.polito.wa2.group17.catalog.exceptions

import it.polito.wa2.group17.common.exception.GenericBadRequestException

class InvalidTokenException(token: String) :
    GenericBadRequestException("Token $token is not valid")
