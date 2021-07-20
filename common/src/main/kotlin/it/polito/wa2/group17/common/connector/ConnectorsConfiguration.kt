package it.polito.wa2.group17.common.connector

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectorsConfiguration {

    @Bean
    fun circuitBreakerRegistry() = CircuitBreakerRegistry.ofDefaults()

    @Bean
    fun retryRegistry() = RetryRegistry.ofDefaults()

}
