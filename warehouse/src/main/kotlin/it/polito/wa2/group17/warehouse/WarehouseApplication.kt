package it.polito.wa2.group17.warehouse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import it.polito.wa2.group17.common.*

@SpringBootApplication(scanBasePackages = ["it.polito.wa2.group17"])
class WarehouseApplication

fun main(args: Array<String>) {
    runApplication<WarehouseApplication>(*args)
}
