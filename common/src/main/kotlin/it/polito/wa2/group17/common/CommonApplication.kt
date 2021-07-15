package it.polito.wa2.group17.common

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CommonApplication{
    init{
        print("Hello")
    }
}

fun main(args: Array<String>) {
    runApplication<CommonApplication>(*args)
}
