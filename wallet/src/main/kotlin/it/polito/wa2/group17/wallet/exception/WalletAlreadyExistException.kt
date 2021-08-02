package it.polito.wa2.group17.wallet.exception

import it.polito.wa2.group17.common.exception.AutoLoggableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class WalletAlreadyExistException(userId: Long) : AutoLoggableException("Wallet already exists for user $userId")
