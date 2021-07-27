package it.polito.wa2.group17.mail

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class MailApplication

fun main(args: Array<String>) {
    runApplication<MailApplication>(*args)
}
