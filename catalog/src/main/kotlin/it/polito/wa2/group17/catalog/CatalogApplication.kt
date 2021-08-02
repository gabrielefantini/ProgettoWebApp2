package it.polito.wa2.group17.catalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import it.polito.wa2.group17.common.*
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication(scanBasePackages = ["it.polito.wa2.group17"])
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class CatalogApplication{

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
}

fun main(args: Array<String>) {
    runApplication<CatalogApplication>(*args)
}
