package it.polito.wa2.group17.wallet

import it.polito.wa2.group17.common.CommonApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WalletApplication

fun main(args: Array<String>) {
    runApplication<WalletApplication>(*args)
}
