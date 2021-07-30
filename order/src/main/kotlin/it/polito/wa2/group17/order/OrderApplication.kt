package it.polito.wa2.group17.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication(scanBasePackages = ["it.polito.wa2.group17"])
@ConfigurationPropertiesScan
@EnableEurekaClient
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}
