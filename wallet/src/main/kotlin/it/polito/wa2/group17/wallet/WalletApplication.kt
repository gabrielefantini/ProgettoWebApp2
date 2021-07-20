package it.polito.wa2.group17.wallet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["it.polito.wa2.group17"])
class WalletApplication

fun main(args: Array<String>) {
    runApplication<WalletApplication>(*args)
}
