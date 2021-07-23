package it.polito.wa2.group17.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["it.polito.wa2.group17"])
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}
