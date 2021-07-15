package it.polito.wa2.group17.catalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CatalogApplication

fun main(args: Array<String>) {
    runApplication<CatalogApplication>(*args)
}
